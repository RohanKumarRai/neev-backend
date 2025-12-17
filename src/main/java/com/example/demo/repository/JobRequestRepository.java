package com.example.demo.repository;

import com.example.demo.model.JobRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRequestRepository extends JpaRepository<JobRequest, Long> {
    List<JobRequest> findByUserId(Long userId);
    List<JobRequest> findByAssignedWorkerId(Long workerId);

}
