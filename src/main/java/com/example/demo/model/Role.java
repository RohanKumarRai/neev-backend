package com.example.demo.model;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    // ⭐️ REQUIRED FIX: Add the roles sent by the frontend's registration form
    ROLE_WORKER,
    ROLE_EMPLOYER
}