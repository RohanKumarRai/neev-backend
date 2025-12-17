package com.example.demo.controller;

import com.example.demo.dto.CreateJobRequest;
import com.example.demo.model.JobRequest;
import com.example.demo.service.JobRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.JobApplicationResponse;


import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobRequestController {

    private final JobRequestService service;

    public JobRequestController(JobRequestService service) {
        this.service = service;
    }

    // ✅ 1. EMPLOYER ONLY — CREATE JOB (USER COMES FROM JWT)
    @PostMapping
    public ResponseEntity<?> createJob(@RequestBody CreateJobRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getPrincipal().toString(); // logged-in employer

        JobRequest job = service.createJob(req, email);
        return ResponseEntity.ok(job);
    }

    // ✅ 2. EMPLOYER — VIEW MY JOBS
    @GetMapping("/my")
    public ResponseEntity<?> getMyJobs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getPrincipal().toString();

        List<JobRequest> list = service.getJobsByEmployer(email);
        return ResponseEntity.ok(list);
    }

    // ✅ 3. ALL LOGGED-IN USERS — VIEW ALL JOBS
    @GetMapping
    public ResponseEntity<?> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    // ✅ 4. VIEW SINGLE JOB
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =====================================================
    // ✅ 5. EMPLOYER — VIEW APPLICATIONS FOR OWN JOB
    // =====================================================
    @GetMapping("/{id}/applications")
    public ResponseEntity<List<JobApplicationResponse>> getApplications(@PathVariable Long id) {


        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        return ResponseEntity.ok(service.getApplicationsForJob(id, email));
    }



    // =====================================================
    // ✅ 6. WORKER — APPLY TO JOB
    // =====================================================
    @PostMapping("/{id}/apply")
    public ResponseEntity<?> apply(@PathVariable Long id,
                                   @RequestBody(required = false) String message) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String workerEmail = auth.getPrincipal().toString();

        try {
            var app = service.applyToJob(id, workerEmail, message);
            return ResponseEntity.ok(app);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Server error while applying");
        }
    }

    // =====================================================
    // ✅ 7. EMPLOYER — ACCEPT APPLICATION
    // =====================================================
    @PostMapping("/applications/{appId}/accept")
    public ResponseEntity<?> accept(@PathVariable Long appId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        service.decideApplication(appId, true, email);
        return ResponseEntity.ok("Application accepted");
    }

    // =====================================================
    // ✅ 8. EMPLOYER — REJECT APPLICATION
    // =====================================================
    @PostMapping("/applications/{appId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long appId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        service.decideApplication(appId, false, email);
        return ResponseEntity.ok("Application rejected");
    }

    // completes job
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeJob(@PathVariable Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        service.completeJob(id, email);
        return ResponseEntity.ok("Job completed");
    }

    @GetMapping("/assigned")
    public ResponseEntity<?> getAssignedJobs() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        return ResponseEntity.ok(service.getJobsForWorker(email));
    }



}
