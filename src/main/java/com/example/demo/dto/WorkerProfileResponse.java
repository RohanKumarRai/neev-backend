package com.example.demo.dto;

import java.time.Instant;
import java.util.List;

public class WorkerProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String skillCategory;
    private Integer experienceYears;
    private Double dailyRate;
    private Double hourlyRate;
    private String location;
    private String bio;
    private String phone;
    private String availability;
    private String audioBioUrl;   // NEW
    private String photoUrl;      // NEW
    private Instant createdAt;

    // NEW: video URLs for portfolio
    private List<String> videoUrls;

    public WorkerProfileResponse() {}

    public WorkerProfileResponse(Long id,
                                 Long userId,
                                 String fullName,
                                 String skillCategory,
                                 Integer experienceYears,
                                 Double dailyRate,
                                 Double hourlyRate,
                                 String location,
                                 String bio,
                                 String phone,
                                 String availability,
                                 String audioBioUrl,
                                 String photoUrl,
                                 Instant createdAt,
                                 List<String> videoUrls) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.skillCategory = skillCategory;
        this.experienceYears = experienceYears;
        this.dailyRate = dailyRate;
        this.hourlyRate = hourlyRate;
        this.location = location;
        this.bio = bio;
        this.phone = phone;
        this.availability = availability;
        this.audioBioUrl = audioBioUrl;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.videoUrls = videoUrls;
    }

    // ---- getters ----
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getSkillCategory() { return skillCategory; }
    public Integer getExperienceYears() { return experienceYears; }
    public Double getDailyRate() { return dailyRate; }
    public Double getHourlyRate() { return hourlyRate; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getAvailability() { return availability; }
    public String getAudioBioUrl() { return audioBioUrl; }
    public String getPhotoUrl() { return photoUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public List<String> getVideoUrls() { return videoUrls; }

    // ---- setters ----
    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setLocation(String location) { this.location = location; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setAudioBioUrl(String audioBioUrl) { this.audioBioUrl = audioBioUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setVideoUrls(List<String> videoUrls) { this.videoUrls = videoUrls; }
}
