package com.example.demo.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recipientUserId;   // Target user
    private String type;            // EX: JOB_ASSIGNED, JOB_APPLIED

    @Lob
    private String message;

    private Boolean read = false;      // unread by default
    private Instant createdAt = Instant.now();
    private Instant readAt;            // NEW

    // optional references
    private Long jobId;
    private Long actorUserId;

    // getters
    public Long getId() { return id; }
    public Long getRecipientUserId() { return recipientUserId; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public Boolean getRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
    public Long getJobId() { return jobId; }
    public Long getActorUserId() { return actorUserId; }

    // setters
    public void setId(Long id) { this.id = id; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }
    public void setType(String type) { this.type = type; }
    public void setMessage(String message) { this.message = message; }
    public void setRead(Boolean read) { this.read = read; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
}
