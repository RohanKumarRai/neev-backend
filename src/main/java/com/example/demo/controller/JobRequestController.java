package com.example.demo.controller;

import com.example.demo.dto.CreateJobRequest;
import com.example.demo.dto.JobApplicationResponse;
import com.example.demo.model.JobRequest;
import com.example.demo.service.JobRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobRequestController {

    private final JobRequestService service;

    public JobRequestController(JobRequestService service) {
        this.service = service;
    }

    // =====================================================
    // EMPLOYER — CREATE JOB
    // =====================================================
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody CreateJobRequest req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        JobRequest job = service.createJob(req, email);
        return ResponseEntity.ok(job);
    }

    // =====================================================
    // EMPLOYER — VIEW MY JOBS
    // =====================================================
    @GetMapping("/my")
    public ResponseEntity<?> getMyJobs() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return ResponseEntity.ok(service.getJobsByEmployer(email));
    }

    // =====================================================
    // ALL LOGGED-IN USERS — VIEW ALL JOBS
    // =====================================================
    @GetMapping
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    // =====================================================
    // VIEW SINGLE JOB
    // =====================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =====================================================
    // EMPLOYER — VIEW APPLICATIONS FOR OWN JOB
    // =====================================================
    @GetMapping("/{id}/applications")
    public ResponseEntity<List<JobApplicationResponse>> getApplications(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return ResponseEntity.ok(service.getApplicationsForJob(id, email));
    }

    // =====================================================
    // WORKER — APPLY TO JOB
    // =====================================================
    @PostMapping("/{id}/apply")
    public ResponseEntity<?> apply(@PathVariable Long id,
                                   @RequestBody(required = false) String message) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String workerEmail = auth.getName();

        try {
            return ResponseEntity.ok(service.applyToJob(id, workerEmail, message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error while applying");
        }
    }

    // =====================================================
    // EMPLOYER — ACCEPT APPLICATION
    // =====================================================
    @PostMapping("/applications/{appId}/accept")
    public ResponseEntity<?> accept(@PathVariable Long appId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        service.decideApplication(appId, true, email);
        return ResponseEntity.ok("Application accepted");
    }

    // =====================================================
    // EMPLOYER — REJECT APPLICATION
    // =====================================================
    @PostMapping("/applications/{appId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long appId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        service.decideApplication(appId, false, email);
        return ResponseEntity.ok("Application rejected");
    }

    // =====================================================
    // EMPLOYER — COMPLETE JOB
    // =====================================================
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeJob(@PathVariable Long id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String employerEmail = auth.getName();

        service.completeJob(id, employerEmail);
        return ResponseEntity.ok("Job completed by employer");
    }

    // =====================================================
    // WORKER — COMPLETE ASSIGNED JOB
    // =====================================================
    @PostMapping("/{jobId}/worker-complete")
    public ResponseEntity<?> workerCompleteJob(@PathVariable Long jobId,
                                               Authentication authentication) {

        String workerEmail = authentication.getName();
        service.markJobCompleted(jobId, workerEmail);

        return ResponseEntity.ok("Job marked as completed by worker");
    }

    // =====================================================
    // WORKER — VIEW ASSIGNED JOBS
    // =====================================================
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedJobs() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return ResponseEntity.ok(service.getJobsForWorker(email));
    }
}
