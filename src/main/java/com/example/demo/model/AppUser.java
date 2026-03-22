package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

// ✅ CHANGES:
//  1. @JsonIgnore added to the `password` field.
//     Previously POST /api/users (register) and GET /api/users/all both serialised the
//     BCrypt hash into the JSON response. Even though it's hashed it should never leave
//     the server. @JsonIgnore removes it from ALL outgoing JSON automatically.
//
//  2. No other logic changed — all existing getters/setters/helpers are identical.

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // ✅ FIX: never serialise the password hash into API responses
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    private String phone;
    private String location;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AppUser() {}

    /* ── GETTERS ── */

    public Long getId()           { return id; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getPassword()   { return password; }  // still available internally
    public Role getRole()         { return role; }
    public String getPhone()      { return phone; }
    public String getLocation()   { return location; }
    public Instant getCreatedAt() { return createdAt; }

    /* ── SETTERS ── */

    public void setId(Long id)       { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }

    /**
     * Accepts "worker", "ROLE_WORKER", "Worker" etc.
     */
    public void setRole(String role) {
        if (role == null || role.isBlank()) {
            this.role = Role.ROLE_USER;
            return;
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) normalized = "ROLE_" + normalized;
        try {
            this.role = Role.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    public void setRole(Role role)   { this.role = role != null ? role : Role.ROLE_USER; }
    public void setPhone(String phone)       { this.phone = phone; }
    public void setLocation(String location) { this.location = location; }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    /* ── DOMAIN HELPERS ── */

    public boolean isWorker()   { return this.role == Role.ROLE_WORKER; }
    public boolean isEmployer() { return this.role == Role.ROLE_EMPLOYER; }
    public boolean isAdmin()    { return this.role == Role.ROLE_ADMIN; }
}
