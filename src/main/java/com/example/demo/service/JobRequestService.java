package com.example.demo.service;

import com.example.demo.dto.CreateJobRequest;
import com.example.demo.dto.JobApplicationResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    // =====================================================
    // ✅ EMPLOYER CREATES JOB
    // =====================================================
    public JobRequest createJob(CreateJobRequest req, String employerEmail) {

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (employer.getRole() != Role.ROLE_EMPLOYER) {
            throw new SecurityException("Only employers can post jobs");
        }

        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new IllegalArgumentException("Job title is required");
        }

        JobRequest job = new JobRequest();
        job.setUserId(employer.getId());
        job.setTitle(req.getTitle());
        job.setDescription(req.getDescription());
        job.setLocation(req.getLocation());
        job.setCategory(req.getCategory());
        job.setJobType(req.getJobType());
        job.setSalary(req.getSalary());
        job.setContactPhone(req.getContactPhone());
        job.setStatus(JobRequest.Status.OPEN);

        return repo.save(job);
    }

    // =====================================================
    // ✅ EMPLOYER — VIEW OWN JOBS
    // =====================================================
    public List<JobRequest> getJobsByEmployer(String employerEmail) {

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        return repo.findByUserId(employer.getId());
    }

    // =====================================================
    // ✅ VIEW ALL JOBS
    // =====================================================
    public List<JobRequest> listAll() {
        return repo.findAll();
    }

    // =====================================================
    // ✅ VIEW SINGLE JOB
    // =====================================================
    public Optional<JobRequest> getById(Long id) {
        return repo.findById(id);
    }

    // =====================================================
    // ✅ WORKER — APPLY TO JOB
    // =====================================================
    public JobApplication applyToJob(Long jobId, String workerEmail, String message) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (job.getStatus() != JobRequest.Status.OPEN) {
            throw new IllegalStateException("Job is not open");
        }

        AppUser user = appUserRepo.findByEmail(workerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.ROLE_WORKER) {
            throw new SecurityException("Only workers can apply to jobs");
        }

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        if (appRepo.existsByJobIdAndWorkerId(jobId, profile.getId())) {
            throw new IllegalStateException("You have already applied to this job");
        }

        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setWorkerId(profile.getId());
        app.setMessage(message);

        JobApplication saved = appRepo.save(app);

        // 🔔 Notify Employer
        try {
            String msg = "New application from \"" + profile.getFullName() +
                    "\" for job: " + job.getTitle();

            notificationService.create(
                    job.getUserId(),
                    "JOB_APPLIED",
                    msg,
                    job.getId(),
                    user.getId()
            );
        } catch (Exception ignored) {}

        return saved;
    }

    // =====================================================
    // ✅ EMPLOYER — VIEW APPLICATIONS (YOUR EXACT STYLE)
    // =====================================================
    public List<JobApplicationResponse> getApplicationsForJob(Long jobId, String employerEmail) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!job.getUserId().equals(employer.getId())) {
            throw new SecurityException("Not your job");
        }

        return appRepo.findByJobId(jobId).stream().map(app -> {

            WorkerProfile profile = workerRepo.findById(app.getWorkerId())
                    .orElseThrow(() -> new IllegalArgumentException("Worker profile missing"));

            return new JobApplicationResponse(app, profile);

        }).toList();
    }

    // =====================================================
    // ✅ EMPLOYER — ACCEPT / REJECT APPLICATION
    // =====================================================
    public void decideApplication(Long appId, boolean accept, String employerEmail) {

        JobApplication app = appRepo.findById(appId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        JobRequest job = repo.findById(app.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!job.getUserId().equals(employer.getId())) {
            throw new SecurityException("Not authorized");
        }

        if (accept) {
            app.setStatus(JobApplication.Status.ACCEPTED);
            job.setStatus(JobRequest.Status.ASSIGNED);
            job.setAssignedWorkerId(app.getWorkerId());
            repo.save(job);
        } else {
            app.setStatus(JobApplication.Status.REJECTED);
        }

        appRepo.save(app);
    }

    // =====================================================
    // ✅ EMPLOYER — COMPLETE JOB
    // =====================================================
    public void completeJob(Long jobId, String employerEmail) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (!job.getUserId().equals(employer.getId())) {
            throw new SecurityException("Not authorized");
        }

        if (job.getStatus() != JobRequest.Status.ASSIGNED) {
            throw new IllegalStateException("Job is not assigned");
        }

        job.setStatus(JobRequest.Status.COMPLETED);
        repo.save(job);
    }

    // =====================================================
    // ✅ WORKER — VIEW ASSIGNED JOBS
    // =====================================================
  
}
