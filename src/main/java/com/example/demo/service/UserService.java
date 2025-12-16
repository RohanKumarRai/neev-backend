package com.example.demo.service;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.config.JwtUtil;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(AppUserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    // ------------------------------------------------------------------
    // UPDATED createUser()  (FULL REPLACEMENT)
    // ------------------------------------------------------------------
    public AppUser createUser(CreateUserRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("CreateUserRequest cannot be null");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Normalize role safely
        String incomingRole = req.getRole();
        if (incomingRole == null || incomingRole.isBlank()) {
            incomingRole = "ROLE_USER";
        } else {
            incomingRole = incomingRole.trim();
            if (!incomingRole.startsWith("ROLE_")) {
                incomingRole = "ROLE_" + incomingRole.toUpperCase();
            }
        }

        // Check for existing email
        Optional<AppUser> existing = userRepository.findByEmail(req.getEmail());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        String hashed = passwordEncoder.encode(req.getPassword());

        AppUser user = new AppUser();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(hashed);

        // Pass normalized role; AppUser#setRole handles enum conversion
        user.setRole(incomingRole);

        return userRepository.save(user);
    }

    // ------------------------------------------------------------------
    // REMAINING CODE UNCHANGED
    // ------------------------------------------------------------------

    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<AppUser> findAllUsers() {
        return userRepository.findAll();
    }

    public LoginResponse login(LoginRequest req) {
        if (req == null || req.getEmail() == null || req.getPassword() == null) {
            return new LoginResponse("failure", "Email and password are required");
        }

        Optional<AppUser> userOpt = userRepository.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            return new LoginResponse("failure", "Invalid email or password");
        }

        AppUser user = userOpt.get();

        String storedHash = user.getPassword();
        if (storedHash == null) {
            return new LoginResponse("failure", "Invalid email or password");
        }

        boolean matches = passwordEncoder.matches(req.getPassword(), storedHash);
        if (!matches) {
            return new LoginResponse("failure", "Invalid email or password");
        }

        String token;
        try {
            token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRole().name()   // ✅ SEND ROLE INTO TOKEN
            );

        } catch (NoSuchMethodError | AbstractMethodError e) {
            return new LoginResponse("success","Login successful, but token generation failed");
        }

        return new LoginResponse(
                "success",
                "Login successful",
                token,
                user.getRole().toString()
        );
    }
}
