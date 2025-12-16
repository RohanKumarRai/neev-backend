package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Development CORS configuration to allow vite frontend (http://localhost:5173) to call backend.
 * For production, restrict/remove or read allowed origins from properties.
 */
@Configuration
public class DevCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        cfg.addAllowedHeader(CorsConfiguration.ALL);
        cfg.addAllowedMethod(CorsConfiguration.ALL);
        cfg.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }
}
