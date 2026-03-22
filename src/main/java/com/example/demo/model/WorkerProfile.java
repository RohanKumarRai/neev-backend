package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// ✅ CHANGES:
//  1. @ElementCollection + @CollectionTable added to videoUrls.
//     Without these annotations, the List<String> field is NOT persisted to the database
//     at all — Spring JPA silently ignores unmapped collection fields. This was causing
//     video URLs to disappear on every restart.
//
//  2. No other logic changed.

@Entity
@Table(name = "worker_profile")
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    private Double averageRating = 0.0;

    // ✅ FIX: @ElementCollection tells JPA to persist this list into a separate table
    //         worker_video_urls(worker_id, url).  Without it the field was ignored entirely.
    @ElementCollection
    @CollectionTable(
            name = "worker_video_urls",
            joinColumns = @JoinColumn(name = "worker_id")
    )
    @Column(name = "url")
    private List<String> videoUrls = new ArrayList<>();

    public WorkerProfile() {}

    /* ── GETTERS ── */

    public Long getId()                  { return id; }
    public AppUser getUser()             { return user; }
    public String getFullName()          { return fullName; }
    public String getSkillCategory()     { return skillCategory; }
    public Integer getExperienceYears()  { return experienceYears; }
    public Double getDailyRate()         { return dailyRate; }
    public Double getHourlyRate()        { return hourlyRate; }
    public String getLocation()          { return location; }
    public String getBio()               { return bio; }
    public String getPhone()             { return phone; }
    public String getAvailability()      { return availability; }
    public Instant getCreatedAt()        { return createdAt; }
    public String getAudioBioUrl()       { return audioBioUrl; }
    public String getPhotoUrl()          { return photoUrl; }
    public Double getAverageRating()     { return averageRating; }
    public List<String> getVideoUrls()   { return videoUrls; }

    /* ── SETTERS ── */

    public void setId(Long id)                           { this.id = id; }
    public void setUser(AppUser user)                    { this.user = user; }
    public void setFullName(String fullName)             { this.fullName = fullName; }
    public void setSkillCategory(String skillCategory)   { this.skillCategory = skillCategory; }
    public void setExperienceYears(Integer v)            { this.experienceYears = v; }
    public void setDailyRate(Double dailyRate)           { this.dailyRate = dailyRate; }
    public void setHourlyRate(Double hourlyRate)         { this.hourlyRate = hourlyRate; }
    public void setLocation(String location)             { this.location = location; }
    public void setBio(String bio)                       { this.bio = bio; }
    public void setPhone(String phone)                   { this.phone = phone; }
    public void setAvailability(String availability)     { this.availability = availability; }
    public void setCreatedAt(Instant createdAt)          { this.createdAt = createdAt; }
    public void setAudioBioUrl(String audioBioUrl)       { this.audioBioUrl = audioBioUrl; }
    public void setPhotoUrl(String photoUrl)             { this.photoUrl = photoUrl; }
    public void setAverageRating(Double averageRating)   { this.averageRating = averageRating; }
    public void setVideoUrls(List<String> videoUrls)     { this.videoUrls = videoUrls; }
}
