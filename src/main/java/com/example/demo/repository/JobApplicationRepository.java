package com.example.demo.repository;

import com.example.demo.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // All applications for a job (employer view)
    List<JobApplication> findByJobId(Long jobId);

    // All applications by a worker (worker dashboard)
    List<JobApplication> findByWorkerId(Long workerId);

    // 🔥 CRITICAL MARKETPLACE RULE
    // Prevent duplicate applications to the same job
    Optional<JobApplication> findByJobIdAndWorkerId(Long jobId, Long workerId);

    // Optional (performance optimization)
    boolean existsByJobIdAndWorkerId(Long jobId, Long workerId);
}
