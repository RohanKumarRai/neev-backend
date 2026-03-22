package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.cors.*;

import java.util.List;

// ✅ CHANGES:
//  1. DevCorsConfig deleted — CORS lives here only (no duplicate CorsFilter bean).
//  2. localhost:5173 added to allowed origins here (was only in DevCorsConfig before).
//  3. GET /api/users/all now requires ROLE_ADMIN (previously any authenticated user could list all users + hashed passwords).
//  4. /api/notifications/** added to the secured-routes list explicitly.

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:5173",       // Vite dev server (was only in DevCorsConfig)
                "http://127.0.0.1:5173",
                "https://monopsy.vercel.app",
                "https://neev-frontend.vercel.app",
                "https://localhost"            // Android WebView
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .securityContext(security -> security.requireExplicitSave(false))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ── PUBLIC ──────────────────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/", "/api/hello").permitAll()

                        // ── ADMIN ONLY ───────────────────────────────────────────
                        // FIX: was .anyRequest().authenticated() which let any logged-in
                        // user hit GET /api/users/all and receive all users incl. password hashes.
                        .requestMatchers(HttpMethod.GET, "/api/users/all").hasAuthority("ROLE_ADMIN")

                        // ── AUTHENTICATED ────────────────────────────────────────
                        .requestMatchers("/api/workers/**").authenticated()
                        .requestMatchers("/api/jobs/**").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/files/**").authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
