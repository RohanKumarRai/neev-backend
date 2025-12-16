package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "worker_profile")
public class WorkerProfile {

    // add near other fields
    private Double averageRating = 0.0;

    // getter + setter
    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to AppUser
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private String fullName;
    private String skillCategory;
    private Integer experienceYears;
    private Double dailyRate;
    private Double hourlyRate;
    private String location;
    private String audioBioUrl;
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String phone;
    private String availability;

    private Instant createdAt = Instant.now();
    private List<String> videoUrls = new ArrayList<>();

    // getter & setter
    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public WorkerProfile() {}

    // getters & setters
    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public String getFullName() { return fullName; }
    public String getSkillCategory() { return skillCategory; }
    public Integer getExperienceYears() { return experienceYears; }
    public Double getDailyRate() { return dailyRate; }
    public Double getHourlyRate() { return hourlyRate; }
    public String getLocation() { return location; }
    public String getBio() { return bio; }
    public String getPhone() { return phone; }
    public String getAvailability() { return availability; }
    public Instant getCreatedAt() { return createdAt; }
    public String getAudioBioUrl() { return audioBioUrl; }
    public void setAudioBioUrl(String audioBioUrl) { this.audioBioUrl = audioBioUrl; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public void setId(Long id) { this.id = id; }
    public void setUser(AppUser user) { this.user = user; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setSkillCategory(String skillCategory) { this.skillCategory = skillCategory; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setLocation(String location) { this.location = location; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
