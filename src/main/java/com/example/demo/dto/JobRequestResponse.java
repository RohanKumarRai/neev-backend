package com.example.demo.dto;

import com.example.demo.model.JobRequest;
import java.time.Instant;

public class JobRequestResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String location;
    private String skillCategory;
    private Double budget;
    private JobRequest.Status status;
    private Long assignedWorkerId;
    private Instant createdAt;

    public JobRequestResponse(Long id, Long userId, String title, String description,
                              String location, String skillCategory, Double budget,
                              JobRequest.Status status, Long assignedWorkerId,
                              Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.skillCategory = skillCategory;
        this.budget = budget;
        this.status = status;
        this.assignedWorkerId = assignedWorkerId;
        this.createdAt = createdAt;
    }

    // ===== Getters required by Jackson =====
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public String getSkillCategory() {
        return skillCategory;
    }

    public Double getBudget() {
        return budget;
    }

    public JobRequest.Status getStatus() {
        return status;
    }

    public Long getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
