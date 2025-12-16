package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_application")
public class JobApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private Long workerId;       // worker (profile) who applied
    private String message;      // optional cover note

    public enum Status { PENDING, REJECTED, ACCEPTED }
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private Instant createdAt = Instant.now();

    // getters & setters
    public Long getId(){return id;}
    public Long getJobId(){return jobId;}
    public void setJobId(Long jobId){this.jobId = jobId;}
    public Long getWorkerId(){return workerId;}
    public void setWorkerId(Long workerId){this.workerId = workerId;}
    public String getMessage(){return message;}
    public void setMessage(String message){this.message = message;}
    public Status getStatus(){return status;}
    public void setStatus(Status status){this.status = status;}
    public Instant getCreatedAt(){return createdAt;}
}
