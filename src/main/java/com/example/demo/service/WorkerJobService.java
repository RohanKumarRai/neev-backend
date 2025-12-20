package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkerJobService {

    private final AppUserRepository appUserRepo;
    private final WorkerProfileRepository workerRepo;
    private final JobRequestRepository jobRepo;

    public WorkerJobService(AppUserRepository appUserRepo,
                            WorkerProfileRepository workerRepo,
                            JobRequestRepository jobRepo) {
        this.appUserRepo = appUserRepo;
        this.workerRepo = workerRepo;
        this.jobRepo = jobRepo;
    }

    public List<JobRequest> getMyAssignedJobs(String email) {

        AppUser user = appUserRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isWorker()) {
            throw new SecurityException("Only workers can view assigned jobs");
        }

        WorkerProfile profile = workerRepo.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Worker profile not found"));

        return jobRepo.findByAssignedWorkerIdAndStatus(
                profile.getId(),
                JobRequest.Status.ASSIGNED
        );
    }
}
