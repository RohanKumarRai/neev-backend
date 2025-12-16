package com.example.demo.dto;

import com.example.demo.model.Role; // ⭐️ REQUIRED IMPORT

public class LoginResponse {
    private String status;
    private String message;
    private String token;
    private String role;

    // 1. Constructor for failure (2 arguments)
    public LoginResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.token = null;
        this.role = null;
    }

    // 2. Constructor accepting String role (for flexibility)
    public LoginResponse(String status, String message, String token, String role) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.role = role;
    }

    // ⭐️ FIX: NEW CONSTRUCTOR - Accepts the Role Enum object (Fixes the UserService error)
    public LoginResponse(String status, String message, String token, Role role) {
        this.status = status;
        this.message = message;
        this.token = token;
        // Convert the Enum object to its String representation (e.g., "ROLE_EMPLOYER")
        this.role = role.toString();
    }

    // Getters
    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getRole() { return role; }
}