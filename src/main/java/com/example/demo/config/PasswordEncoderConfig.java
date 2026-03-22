package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// ✅ NEW FILE.
//
// Previously UserService did `new BCryptPasswordEncoder()` manually inside its constructor.
// This is bad practice because:
//  - You cannot swap the algorithm (e.g. to Argon2) without touching business logic.
//  - Spring cannot manage or mock the encoder in tests.
//
// Now it's a proper @Bean. UserService, and any future service that needs to hash passwords,
// simply @Autowires PasswordEncoder.

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
