package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.config.JwtUtil;

import java.util.List;
import java.util.Optional;

// ✅ CHANGES:
//  1. PasswordEncoder is now injected (not instantiated with `new BCryptPasswordEncoder()`).
//     The bean is declared in config/PasswordEncoderConfig.java.
//     This allows the algorithm to be swapped in one place, and makes the service testable
//     (you can inject a NoOpPasswordEncoder in unit tests without reflection hacks).
//
//  2. All business logic is otherwise identical.

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;   // ✅ injected, not newed
    private final JwtUtil jwtUtil;

    public UserService(AppUserRepository userRepository,
                       PasswordEncoder passwordEncoder,   // ✅ Spring injects BCrypt bean
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AppUser createUser(CreateUserRequest req) {
        if (req == null) throw new IllegalArgumentException("Request cannot be null");
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (req.getPassword() == null || req.getPassword().isBlank())
            throw new IllegalArgumentException("Password is required");

        String incomingRole = req.getRole();
        if (incomingRole == null || incomingRole.isBlank()) {
            incomingRole = "ROLE_USER";
        } else {
            incomingRole = incomingRole.trim();
            if (!incomingRole.startsWith("ROLE_"))
                incomingRole = "ROLE_" + incomingRole.toUpperCase();
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        AppUser user = new AppUser();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(incomingRole);

        return userRepository.save(user);
    }

    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<AppUser> findAllUsers() {
        return userRepository.findAll();
    }

    public LoginResponse login(LoginRequest req) {
        if (req == null || req.getEmail() == null || req.getPassword() == null)
            return new LoginResponse("failure", "Email and password are required");

        Optional<AppUser> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty())
            return new LoginResponse("failure", "Invalid email or password");

        AppUser user = userOpt.get();
        if (user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword()))
            return new LoginResponse("failure", "Invalid email or password");

        String token;
        try {
            token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        } catch (NoSuchMethodError | AbstractMethodError e) {
            return new LoginResponse("success", "Login successful, but token generation failed");
        }

        return new LoginResponse("success", "Login successful", token, user.getRole().toString());
    }
}
