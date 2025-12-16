package com.example.demo.dto;

import com.example.demo.model.Role;

public class CreateUserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;

    public CreateUserResponse() {}

    public CreateUserResponse(Long id, String name, String email, Role role) {
        this.id = id; this.name = name; this.email = email; this.role = role;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
}
