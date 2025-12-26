package com.example.demo.service;

import com.example.demo.dto.CreateJobRequest;
import com.example.demo.dto.JobApplicationResponse;
import com.example.demo.model.*;
import com.example.demo.repository.*;

import jakarta.transaction.Transactional;
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
    // EMPLOYER — CREATE JOB
    // =====================================================
    public JobRequest createJob(CreateJobRequest req, String employerEmail) {

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        if (employer.getRole() != Role.ROLE_EMPLOYER) {
            throw new SecurityException("Only employers can post jobs");
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
        job.setLatitude(req.getLatitude());
        job.setLongitude(req.getLongitude());
        job.setStatus(JobRequest.Status.OPEN);

        return repo.save(job);
    }

    // =====================================================
    // EMPLOYER — VIEW OWN JOBS
    // =====================================================
    public List<JobRequest> getJobsByEmployer(String employerEmail) {

        AppUser employer = appUserRepo.findByEmail(employerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

        return repo.findByUserId(employer.getId());
    }

    // =====================================================
    // VIEW ALL JOBS
    // =====================================================
    public List<JobRequest> listAll() {
        return repo.findAll();
    }

    // =====================================================
    // VIEW SINGLE JOB
    // =====================================================
    public Optional<JobRequest> getById(Long id) {
        return repo.findById(id);
    }

    // =====================================================
    // WORKER — APPLY TO JOB
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
            throw new SecurityException("Only workers can apply");
        }

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        if (appRepo.existsByJobIdAndWorkerId(jobId, profile.getId())) {
            throw new IllegalStateException("Already applied");
        }

        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setWorkerId(profile.getId());
        app.setMessage(message);

        return appRepo.save(app);
    }

    // =====================================================
    // EMPLOYER — VIEW APPLICATIONS
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
                    .orElseThrow();
            return new JobApplicationResponse(app, profile);
        }).toList();
    }

    // =====================================================
    // EMPLOYER — ACCEPT / REJECT APPLICATION
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
    // EMPLOYER — COMPLETE JOB
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
    // ✅ WORKER — MARK JOB COMPLETED (CORE FIX)
    // =====================================================
    @Transactional
    public void markJobCompleted(Long jobId, String workerEmail) {

        JobRequest job = repo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ✅ Must be assigned
        if (job.getStatus() != JobRequest.Status.ASSIGNED) {
            throw new RuntimeException("Job is not assigned");
        }

        AppUser user = appUserRepo.findByEmail(workerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ROLE_WORKER) {
            throw new RuntimeException("Only workers can complete jobs");
        }

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Worker profile not found"));

        // ✅ Worker must match
        if (job.getAssignedWorkerId() == null ||
                !job.getAssignedWorkerId().equals(profile.getId())) {
            throw new RuntimeException("Not authorized to complete this job");
        }

        // ✅ Mark completed
        job.setStatus(JobRequest.Status.COMPLETED);
        repo.save(job);
    }

    // =====================================================
    // WORKER — VIEW ASSIGNED JOBS
    // =====================================================
    public List<JobRequest> getJobsForWorker(String workerEmail) {

        AppUser user = appUserRepo.findByEmail(workerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.ROLE_WORKER) {
            throw new SecurityException("Only workers allowed");
        }

        WorkerProfile profile = workerRepo.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        return repo.findByAssignedWorkerId(profile.getId());
    }
}
