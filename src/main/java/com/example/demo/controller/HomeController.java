package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // Root mapping so '/' no longer returns Whitelabel 404
    @GetMapping("/")
    public String home() {
        return "Welcome to Neev — backend is running";
    }

    // Simple API test endpoint
    @GetMapping("/api/hello")
    public String hello() {
        return "Neev backend ok";
    }
}
