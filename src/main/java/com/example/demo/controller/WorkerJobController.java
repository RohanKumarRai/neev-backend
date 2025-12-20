package com.example.demo.controller;

import com.example.demo.model.JobRequest;
import com.example.demo.service.WorkerJobService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
public class WorkerJobController {

    private final WorkerJobService service;

    public WorkerJobController(WorkerJobService service) {
        this.service = service;
    }

    @GetMapping("/my-jobs")
    public List<JobRequest> getMyAssignedJobs(Authentication authentication) {

        String email = authentication.getName(); // from JWT

        return service.getMyAssignedJobs(email);
    }
}
