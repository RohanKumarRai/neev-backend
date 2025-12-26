package com.example.demo.dto;

public class CreateJobRequest {

    private String title;
    private String description;
    private String location;

    // Fixed category list (Plumber, Electrician, etc.)
    private String category;

    // FULL_TIME, PART_TIME, DAILY
    private String jobType;

    // Salary as text: "₹800/day", "₹120/hour", "₹15000/month"
    private String salary;

    // Employer contact number
    private String contactPhone;

    // ✅ MAP COORDINATES
    private Double latitude;
    private Double longitude;

    // -------- GETTERS --------

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

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    // -------- SETTERS --------

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

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
