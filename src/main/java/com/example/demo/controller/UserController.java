package com.example.demo.controller;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.model.AppUser;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users") // Base mapping: /api/users
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ------------------------------------------------------------------
    // 1. REGISTRATION ENDPOINT: POST /api/users
    // ------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> registerUser(@RequestBody CreateUserRequest request) {
        try {
            AppUser newUser = userService.createUser(request);
            return new ResponseEntity<>(newUser, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // known validation errors
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new LoginResponse("failure", e.getMessage()));
        } catch (Exception e) {
            // unexpected errors: log full stacktrace and return message string so frontend can show it
            e.printStackTrace();
            String msg = e.getMessage() == null ? "Unknown server error" : e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LoginResponse("failure", msg));
        }
    }

    // ------------------------------------------------------------------
    // 2. LOGIN ENDPOINT: POST /api/users/login
    // ------------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);

        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ------------------------------------------------------------------
    // 3. SECURED ENDPOINT: GET /api/users/all (For testing/Admin view)
    // ------------------------------------------------------------------
    @GetMapping("/all")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        List<AppUser> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }
}
