package com.example.demo.dto;

import com.example.demo.model.JobApplication;
import com.example.demo.model.WorkerProfile;

import java.time.Instant;

public class JobApplicationResponse {

    private Long applicationId;
    private String status;
    private String message;

    private Long workerId;
    private String workerName;
    private String skillCategory;
    private String location;
    private int experienceYears;
    private double dailyRate;

    public JobApplicationResponse(JobApplication app, WorkerProfile profile) {
        this.applicationId = app.getId();
        this.status = app.getStatus().name();
        this.message = app.getMessage();

        this.workerId = profile.getId();
        this.workerName = profile.getFullName();
        this.skillCategory = profile.getSkillCategory();
        this.location = profile.getLocation();
        this.experienceYears = profile.getExperienceYears();
        this.dailyRate = profile.getDailyRate();
    }

    public JobApplicationResponse(Long id, Long jobId, String fullName, String skillCategory, String location, Integer experienceYears, Double dailyRate, String message, JobApplication.Status status, Instant createdAt) {
    }

    public Long getApplicationId() { return applicationId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }

    public Long getWorkerId() { return workerId; }
    public String getWorkerName() { return workerName; }
    public String getSkillCategory() { return skillCategory; }
    public String getLocation() { return location; }
    public int getExperienceYears() { return experienceYears; }
    public double getDailyRate() { return dailyRate; }
}
