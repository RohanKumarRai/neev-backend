package com.example.demo.dto;

import com.example.demo.model.Role;

public class LoginResponse {

    private String status;
    private String message;
    private String token;
    private String role;

    // Failure response
    public LoginResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.token = null;
        this.role = null;
    }

    // Success response with role as String
    public LoginResponse(String status, String message, String token, String role) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.role = role;
    }

    // ⭐️ Success response with Role enum (SAFE)
    public LoginResponse(String status, String message, String token, Role role) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.role = role.name(); // ✅ FIXED
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}
