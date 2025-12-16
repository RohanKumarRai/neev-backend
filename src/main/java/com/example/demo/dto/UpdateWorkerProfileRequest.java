package com.example.demo.dto;

public class UpdateWorkerProfileRequest {
    private String fullName;
    private String skillCategory;
    private Integer experienceYears;
    private Double dailyRate;
    private Double hourlyRate;
    private String location;
    private String bio;
    private String phone;
    private String availability;

    public UpdateWorkerProfileRequest() {}
    // getters & setters (same as fields)
    public String getFullName() { return fullName; }
    public String getSkillCategory() { return skillCategory; }
    public Integer getExperienceYears() { return experienceYears; }
    public Double getDailyRate() { return dailyRate; }
    public Double getHourlyRate() { return hourlyRate; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getAvailability() { return availability; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setLocation(String location) { this.location = location; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvailability(String availability) { this.availability = availability; }
}
