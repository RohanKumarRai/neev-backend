package com.example.demo.repository;

import com.example.demo.model.JobRequest;
import com.example.demo.model.JobRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {

    List<JobRequest> findByUserId(Long userId);

    List<JobRequest> findByAssignedWorkerId(Long workerId);

    // ✅ NEW (for B1)
    List<JobRequest> findByAssignedWorkerIdAndStatus(Long workerId, Status status);
}
