package com.example.demo.dto;

public class CreateWorkerProfileRequest {

    private String fullName;
    private String skillCategory;
    private Integer experienceYears;
    private Double dailyRate;
    private Double hourlyRate;
    private String location;
    private String bio;
    private String phone;
    private String availability;

    public CreateWorkerProfileRequest() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSkillCategory() { return skillCategory; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }

    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}
