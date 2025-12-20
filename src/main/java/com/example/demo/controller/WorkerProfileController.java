package com.example.demo.controller;

import com.example.demo.dto.CreateWorkerProfileRequest;
import com.example.demo.dto.UpdateWorkerProfileRequest;
import com.example.demo.dto.WorkerProfileResponse;
import com.example.demo.model.AppUser;
import com.example.demo.model.WorkerProfile;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.WorkerProfileService;
import com.example.demo.service.StorageService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workers")
public class WorkerProfileController {

    private final WorkerProfileService service;
    private final StorageService storageService;
    private final AppUserRepository appUserRepo;

    public WorkerProfileController(WorkerProfileService service,
                                   StorageService storageService,
                                   AppUserRepository appUserRepo) {
        this.service = service;
        this.storageService = storageService;
        this.appUserRepo = appUserRepo;
    }

    @GetMapping("/test")
    public String test() {
        return "controller-loaded";
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateWorkerProfileRequest req) {
        try {
            WorkerProfile p = service.create(req);
            return ResponseEntity.ok(toResponse(p));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Server error: " + ex.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Long userId) {
        return service.getByUserId(userId)
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =====================================================
    // ✅ GET LOGGED-IN WORKER PROFILE (JWT SAFE)
    // =====================================================
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // ✅ FIXED

        AppUser user = appUserRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return service.getByUserId(user.getId())
                .map(p -> ResponseEntity.ok(toResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> listAll() {
        List<WorkerProfileResponse> list = service.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody UpdateWorkerProfileRequest req) {
        try {
            WorkerProfile updated = service.update(id, req);
            return ResponseEntity.ok(toResponse(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // =====================================================
    // MEDIA UPLOAD
    // =====================================================
    @PostMapping("/{id}/media")
    public ResponseEntity<?> uploadMedia(
            @PathVariable Long id,
            @RequestPart(name = "audio", required = false) MultipartFile audio,
            @RequestPart(name = "photo", required = false) MultipartFile photo) {
        try {
            var opt = service.getById(id);
            if (opt.isEmpty()) return ResponseEntity.notFound().build();
            var profile = opt.get();

            if (audio != null && !audio.isEmpty()) {
                String audioUrl = storageService.upload(audio, "workers/" + id + "/audio");
                profile.setAudioBioUrl(audioUrl);
            }
            if (photo != null && !photo.isEmpty()) {
                String photoUrl = storageService.upload(photo, "workers/" + id + "/photo");
                profile.setPhotoUrl(photoUrl);
            }

            var updated = service.updateMedia(profile);
            return ResponseEntity.ok(toResponse(updated));

        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Upload failed: " + ex.getMessage());
        }
    }

    // =====================================================
    // RESPONSE MAPPER
    // =====================================================
    private WorkerProfileResponse toResponse(WorkerProfile p) {
        if (p == null) return null;

        WorkerProfileResponse resp = new WorkerProfileResponse();

        resp.setId(p.getId());
        resp.setUserId(p.getUser() != null ? p.getUser().getId() : null);

        resp.setFullName(
                p.getFullName() != null
                        ? p.getFullName()
                        : (p.getUser() != null ? p.getUser().getName() : null)
        );

        resp.setSkillCategory(p.getSkillCategory());
        resp.setExperienceYears(p.getExperienceYears());
        resp.setDailyRate(p.getDailyRate());
        resp.setHourlyRate(p.getHourlyRate());
        resp.setLocation(p.getLocation());
        resp.setBio(p.getBio());
        resp.setPhone(p.getPhone());
        resp.setAvailability(p.getAvailability());

        resp.setAudioBioUrl(p.getAudioBioUrl());
        resp.setPhotoUrl(p.getPhotoUrl());

        resp.setCreatedAt(p.getCreatedAt() != null ? p.getCreatedAt() : Instant.now());

        resp.setVideoUrls(p.getVideoUrls());

        return resp;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String location) {

        return ResponseEntity.ok(
                service.search(skill, location)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filter(
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) Integer minExp,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) String availability) {

        return ResponseEntity.ok(
                service.filter(skill, minExp, maxRate, availability)
                        .stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList())
        );
    }
}
