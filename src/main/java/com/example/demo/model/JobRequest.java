package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class JobRequest {

    public enum Status {
        OPEN,
        ASSIGNED,
        IN_PROGRESS,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Employer user ID (from JWT)
    private Long userId;

    private String title;

    @Column(length = 2000)
    private String description;

    private String location;

    // ✅ NEW FIELDS (PHASE 3 LOCKED)
    private String category;       // Plumber, Electrician, etc.
    private String jobType;        // FULL_TIME, PART_TIME, DAILY
    private String salary;         // ₹800/day, ₹120/hour, ₹15000/month
    private String contactPhone;  // Employer phone number

    @Enumerated(EnumType.STRING)
    private Status status;

    // ✅ Worker profile ID when assigned
    private Long assignedWorkerId;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ---------------- GETTERS ----------------

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

    public String getCategory() {
        return category;
    }

    public String getJobType() {
        return jobType;
    }

    public String getSalary() {
        return salary;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public Status getStatus() {
        return status;
    }

    public Long getAssignedWorkerId() {
        return assignedWorkerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ---------------- SETTERS ----------------

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setAssignedWorkerId(Long assignedWorkerId) {
        this.assignedWorkerId = assignedWorkerId;
    }
}
