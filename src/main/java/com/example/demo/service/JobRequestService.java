package com.example.demo.service;

import com.example.demo.dto.CreateJobRequest;
import com.example.demo.dto.JobApplicationResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// ✅ CHANGES:
//
//  1. @Transactional added to decideApplication(), completeJob(), markJobCompleted().
//     Previously these methods did multiple repo.save() calls WITHOUT a transaction.
//     If the second save failed, the first was already committed — leaving the DB in a
//     half-updated state (e.g. application ACCEPTED but job still OPEN).
//
//  2. NotificationService.create() is now called after key state transitions:
//       - Worker applies  → employer gets "NEW_APPLICATION" notification
//       - Application accepted → worker gets "APPLICATION_ACCEPTED" notification
//       - Application rejected → worker gets "APPLICATION_REJECTED" notification
//       - Job completed (either side) → other party gets "JOB_COMPLETED" notification
//     Previously the notification infrastructure existed but was never triggered from here.
//
//  3. Exception types made consistent throughout:
//       - "not found"     → IllegalArgumentException  (maps to 400 in controller)
//       - "not authorized"→ SecurityException         (maps to 403 in controller)
//       - "wrong state"   → IllegalStateException     (maps to 409 / 400 in controller)
//     Previously markJobCompleted() threw bare RuntimeException while everything else
//     threw typed exceptions, making global error handling impossible.

@Service
public class JobRequestService {

    private final JobRequestRepository repo;
    private final WorkerProfileRepository workerRepo;
    private final JobApplicationRepository appRepo;
    private final AppUserRepository appUserRepo;
    private final NotificationService notificationService;

    public JobRequestService(JobRequestRepository repo,
                             WorkerProfileRepository workerRepo,
                             JobApplicationRepository appRepo,
                             AppUserRepository appUserRepo,
                             NotificationService notificationService) {
        this.repo = repo;
        this.workerRepo = workerRepo;
        this.appRepo = appRepo;
        this.appUserRepo = appUserRepo;
        this.notificationService = notificationService;
    }

    // ── CREATE JOB ──────────────────────────────────────────────────────────

    @Transactional
    public JobRequest createJob(CreateJobRequest req, String employerEmail) {

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!employer.isEmployer())
            throw new SecurityException("Only employers can post jobs");

        JobRequest job = new JobRequest();
        job.setUserId(employer.getId());
        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setLocation(req.getLocation());
        job.setCategory(req.getCategory());
        job.setJobType(req.getJobType());
        job.setSalary(req.getSalary());
        job.setContactPhone(req.getContactPhone());
        job.setLatitude(req.getLatitude());
        job.setLongitude(req.getLongitude());
        job.setStatus(JobRequest.Status.OPEN);

        return repo.save(job);
    }

    // ── VIEW OWN JOBS (EMPLOYER) ─────────────────────────────────────────────

    public List<JobRequest> getJobsByEmployer(String employerEmail) {
        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));
        return repo.findByUserId(employer.getId());
    }

    // ── LIST ALL ─────────────────────────────────────────────────────────────

    public List<JobRequest> listAll() {
        return repo.findAll();
    }

    // ── SINGLE JOB ───────────────────────────────────────────────────────────

    public Optional<JobRequest> getById(Long id) {
        return repo.findById(id);
    }

    // ── APPLY TO JOB (WORKER) ────────────────────────────────────────────────

    @Transactional
    public JobApplication applyToJob(Long jobId, String workerEmail, String message) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getStatus() != JobRequest.Status.OPEN)
            throw new IllegalStateException("Job is not open for applications");

        AppUser user = appUserRepo.findByEmail(workerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isWorker())
            throw new SecurityException("Only workers can apply to jobs");

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        if (appRepo.existsByJobIdAndWorkerId(jobId, profile.getId()))
            throw new IllegalStateException("You have already applied to this job");

        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setWorkerId(profile.getId());
        app.setMessage(message);
        JobApplication saved = appRepo.save(app);

        // ✅ FIX: notify the employer that a new application arrived
        notificationService.create(
                job.getUserId(),
                "NEW_APPLICATION",
                profile.getFullName() + " applied to your job: " + job.getTitle(),
                jobId,
                user.getId()
        );

        return saved;
    }

    // ── VIEW APPLICATIONS FOR JOB (EMPLOYER) ────────────────────────────────

    public List<JobApplicationResponse> getApplicationsForJob(Long jobId, String employerEmail) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!job.getUserId().equals(employer.getId()))
            throw new SecurityException("You are not the owner of this job");

        return appRepo.findByJobId(jobId).stream().map(app -> {
            WorkerProfile profile = workerRepo.findById(app.getWorkerId())
                    .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));
            return new JobApplicationResponse(app, profile);
        }).toList();
    }

    // ── ACCEPT / REJECT APPLICATION (EMPLOYER) ──────────────────────────────

    // ✅ FIX: @Transactional ensures both saves (application + job) commit together.
    //         If the second save fails, the first is rolled back automatically.
    @Transactional
    public void decideApplication(Long appId, boolean accept, String employerEmail) {

        JobApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        JobRequest job = repo.findById(app.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!job.getUserId().equals(employer.getId()))
            throw new SecurityException("You are not authorised to decide this application");

        // Resolve the worker's AppUser so we can notify them
        WorkerProfile workerProfile = workerRepo.findById(app.getWorkerId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));
        Long workerUserId = workerProfile.getUser().getId();

        if (accept) {
            app.setStatus(JobApplication.Status.ACCEPTED);
            job.setStatus(JobRequest.Status.ASSIGNED);
            job.setAssignedWorkerId(app.getWorkerId());
            repo.save(job);

            // ✅ FIX: notify the worker their application was accepted
            notificationService.create(
                    workerUserId,
                    "APPLICATION_ACCEPTED",
                    "Your application for \"" + job.getTitle() + "\" was accepted!",
                    job.getId(),
                    employer.getId()
            );
        } else {
            app.setStatus(JobApplication.Status.REJECTED);

            // ✅ FIX: notify the worker their application was rejected
            notificationService.create(
                    workerUserId,
                    "APPLICATION_REJECTED",
                    "Your application for \"" + job.getTitle() + "\" was not selected.",
                    job.getId(),
                    employer.getId()
            );
        }

        appRepo.save(app);
    }

    // ── COMPLETE JOB (EMPLOYER SIDE) ─────────────────────────────────────────

    // ✅ FIX: @Transactional + consistent exception types
    @Transactional
    public void completeJob(Long jobId, String employerEmail) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!job.getUserId().equals(employer.getId()))
            throw new SecurityException("You are not authorised to complete this job");

        if (job.getStatus() != JobRequest.Status.ASSIGNED)
            throw new IllegalStateException("Job must be in ASSIGNED state to be completed");

        job.setStatus(JobRequest.Status.COMPLETED);
        repo.save(job);

        // Notify assigned worker
        if (job.getAssignedWorkerId() != null) {
            workerRepo.findById(job.getAssignedWorkerId()).ifPresent(wp -> {
                notificationService.create(
                        wp.getUser().getId(),
                        "JOB_COMPLETED",
                        "The employer has marked \"" + job.getTitle() + "\" as completed.",
                        jobId,
                        employer.getId()
                );
            });
        }
    }

    // ── COMPLETE JOB (WORKER SIDE) ───────────────────────────────────────────

    // ✅ FIX: @Transactional + consistent exception types (was throwing bare RuntimeException)
    @Transactional
    public void markJobCompleted(Long jobId, String workerEmail) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getStatus() != JobRequest.Status.ASSIGNED)
            throw new IllegalStateException("Job must be in ASSIGNED state to be completed");

        AppUser user = appUserRepo.findByEmail(workerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isWorker())
            throw new SecurityException("Only workers can mark jobs as completed");

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        if (job.getAssignedWorkerId() == null || !job.getAssignedWorkerId().equals(profile.getId()))
            throw new SecurityException("You are not the assigned worker for this job");

        job.setStatus(JobRequest.Status.COMPLETED);
        repo.save(job);

        // Notify the employer
        notificationService.create(
                job.getUserId(),
                "JOB_COMPLETED",
                "Worker has marked \"" + job.getTitle() + "\" as completed.",
                jobId,
                user.getId()
        );
    }

    // ── VIEW ASSIGNED JOBS (WORKER) ──────────────────────────────────────────

    public List<JobRequest> getJobsForWorker(String workerEmail) {
        AppUser user = appUserRepo.findByEmail(workerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isWorker())
            throw new SecurityException("Only workers can view assigned jobs");

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        return repo.findByAssignedWorkerId(profile.getId());
    }
}
