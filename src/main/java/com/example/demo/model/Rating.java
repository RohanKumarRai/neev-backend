package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rating")
public class Rating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jobId;
    private Long raterUserId;   // who gave the rating (AppUser.id)
    private Long targetUserId;  // who is being rated (AppUser.id)
    private int rating;         // 1..5
    private String feedback;
    private Instant createdAt = Instant.now();

    // getters / setters
    public Long getId(){return id;}
    public Long getJobId(){return jobId;} public void setJobId(Long jobId){this.jobId = jobId;}
    public Long getRaterUserId(){return raterUserId;} public void setRaterUserId(Long id){this.raterUserId = id;}
    public Long getTargetUserId(){return targetUserId;} public void setTargetUserId(Long id){this.targetUserId = id;}
    public int getRating(){return rating;} public void setRating(int r){this.rating = r;}
    public String getFeedback(){return feedback;} public void setFeedback(String f){this.feedback = f;}
    public Instant getCreatedAt(){return createdAt;}
}
