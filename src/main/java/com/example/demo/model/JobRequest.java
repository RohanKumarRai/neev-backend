package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class JobRequest {

    public enum Status {
        OPEN,
        ASSIGNED,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employer user ID
    private Long userId;

    private String title;

    @Column(length = 2000)
    private String description;

    private String location;

    // Job details
    private String category;
    private String jobType;
    private String salary;
    private String contactPhone;

    // Map coordinates
    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    private Status status;

    // Assigned worker profile ID
    private Long assignedWorkerId;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ================= GETTERS =================

    public Long getId() { return id; }

    public Long getUserId() { return userId; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getLocation() { return location; }

    public String getCategory() { return category; }

    public String getJobType() { return jobType; }

    public String getSalary() { return salary; }

    public String getContactPhone() { return contactPhone; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public Status getStatus() { return status; }

    public Long getAssignedWorkerId() { return assignedWorkerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ================= SETTERS =================

    public void setUserId(Long userId) { this.userId = userId; }

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setLocation(String location) { this.location = location; }

    public void setCategory(String category) { this.category = category; }

    public void setJobType(String jobType) { this.jobType = jobType; }

    public void setSalary(String salary) { this.salary = salary; }

    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public void setStatus(Status status) { this.status = status; }

    public void setAssignedWorkerId(Long assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }
}
