package com.example.demo.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // NOTE: Replace with a secure 32+ char secret for production
    private static final String SECRET = "THIS_IS_A_SECRET_KEY_CHANGE_IT_TO_32+_CHARS_LONG";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 24 hours validity (ms)
    private final long EXPIRATION = 24 * 60 * 60 * 1000L;

    // Generate token using subject (email/username)
    public String generateToken(String subject, String role) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)   // ✅ ADD ROLE
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }



    // Validate token (checks signature + expiry)
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    // Extract subject (email/username)
    public String extractSubject(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
