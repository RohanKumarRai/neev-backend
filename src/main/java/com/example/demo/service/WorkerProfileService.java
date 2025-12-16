package com.example.demo.service;

import com.example.demo.dto.CreateWorkerProfileRequest;
import com.example.demo.dto.UpdateWorkerProfileRequest;
import com.example.demo.model.AppUser;
import com.example.demo.model.WorkerProfile;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.WorkerProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class WorkerProfileService {

    private final WorkerProfileRepository profileRepository;
    private final AppUserRepository userRepository;

    public WorkerProfileService(WorkerProfileRepository profileRepository,
                                AppUserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    /* =========================
       SEARCH & FILTER
       ========================= */

    public List<WorkerProfile> search(String skill, String location) {
        String skillQ = (skill == null) ? "" : skill.trim();
        String locQ = (location == null) ? "" : location.trim();
        return profileRepository
                .findBySkillCategoryIgnoreCaseContainingAndLocationIgnoreCaseContaining(
                        skillQ, locQ
                );
    }

    public List<WorkerProfile> filter(String skill,
                                      Integer minExp,
                                      Double maxRate,
                                      String availability) {

        List<WorkerProfile> base;

        if (skill != null && !skill.isBlank()) {
            base = profileRepository
                    .findBySkillCategoryIgnoreCaseContainingAndLocationIgnoreCaseContaining(
                            skill, ""
                    );
        } else {
            base = profileRepository.findAll();
        }

        return base.stream()
                .filter(p -> {
                    if (minExp != null) {
                        Integer exp = p.getExperienceYears();
                        if (exp == null || exp < minExp) return false;
                    }
                    if (maxRate != null) {
                        Double rate = p.getDailyRate();
                        if (rate == null || rate > maxRate) return false;
                    }
                    if (availability != null && !availability.isBlank()) {
                        String av = p.getAvailability();
                        if (av == null || !av.toLowerCase().contains(availability.toLowerCase()))
                            return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /* =========================
       CREATE (JWT BASED)
       ========================= */

    public WorkerProfile create(CreateWorkerProfileRequest req) {

        if (req == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        // 🔐 Get logged-in user email from JWT
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString();

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // prevent duplicate profile per user
        profileRepository.findByUser(user).ifPresent(p -> {
            throw new IllegalArgumentException("Worker profile already exists");
        });

        WorkerProfile p = new WorkerProfile();
        p.setUser(user);
        p.setFullName(req.getFullName());
        p.setSkillCategory(req.getSkillCategory());
        p.setExperienceYears(req.getExperienceYears());
        p.setDailyRate(req.getDailyRate());
        p.setHourlyRate(req.getHourlyRate());
        p.setLocation(req.getLocation());
        p.setBio(req.getBio());
        p.setPhone(req.getPhone());
        p.setAvailability(req.getAvailability());

        return profileRepository.save(p);
    }

    /* =========================
       READ
       ========================= */

    public Optional<WorkerProfile> getById(Long id) {
        return profileRepository.findById(id);
    }

    public Optional<WorkerProfile> getByUserId(Long userId) {
        AppUser user = userRepository.findById(userId).orElse(null);
        if (user == null) return Optional.empty();
        return profileRepository.findByUser(user);
    }

    public List<WorkerProfile> listAll() {
        return profileRepository.findAll();
    }

    /* =========================
       UPDATE
       ========================= */

    public WorkerProfile update(Long id, UpdateWorkerProfileRequest req) {
        WorkerProfile p = profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        if (req.getFullName() != null) p.setFullName(req.getFullName());
        if (req.getSkillCategory() != null) p.setSkillCategory(req.getSkillCategory());
        if (req.getExperienceYears() != null) p.setExperienceYears(req.getExperienceYears());
        if (req.getDailyRate() != null) p.setDailyRate(req.getDailyRate());
        if (req.getHourlyRate() != null) p.setHourlyRate(req.getHourlyRate());
        if (req.getLocation() != null) p.setLocation(req.getLocation());
        if (req.getBio() != null) p.setBio(req.getBio());
        if (req.getPhone() != null) p.setPhone(req.getPhone());
        if (req.getAvailability() != null) p.setAvailability(req.getAvailability());

        return profileRepository.save(p);
    }

    /* =========================
       MEDIA UPDATE
       ========================= */

    public WorkerProfile updateMedia(WorkerProfile profile) {
        return profileRepository.save(profile);
    }
}
