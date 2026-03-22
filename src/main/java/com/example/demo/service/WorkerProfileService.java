package com.example.demo.service;

import com.example.demo.dto.CreateWorkerProfileRequest;
import com.example.demo.dto.UpdateWorkerProfileRequest;
import com.example.demo.model.AppUser;
import com.example.demo.model.WorkerProfile;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.WorkerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// ✅ CHANGES:
//
//  1. SecurityContextHolder removed from create().
//     Services must NOT access HTTP/security context — that is the controller's
//     responsibility. The authenticated email is now passed in as a parameter,
//     consistent with how every other service (JobRequestService, UserService, etc.)
//     already works.
//     WorkerProfileController.create() is updated to pass auth.getName() through.
//
//  2. getByUserId() simplified: uses findByUserId() directly instead of first
//     fetching the AppUser object unnecessarily.
//
//  3. @Transactional added to create() and update() so partial writes are rolled back.
//
//  4. filter() in-memory limitation documented clearly. A proper JPA Specification
//     approach is noted — replace when the worker table grows beyond ~1000 rows.

@Service
public class WorkerProfileService {

    private final WorkerProfileRepository profileRepository;
    private final AppUserRepository userRepository;

    public WorkerProfileService(WorkerProfileRepository profileRepository,
                                AppUserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    // ── SEARCH ───────────────────────────────────────────────────────────────

    public List<WorkerProfile> search(String skill, String location) {
        String skillQ = (skill == null) ? "" : skill.trim();
        String locQ   = (location == null) ? "" : location.trim();
        return profileRepository
                .findBySkillCategoryIgnoreCaseContainingAndLocationIgnoreCaseContaining(
                        skillQ, locQ);
    }

    // NOTE on filter(): currently fetches matching rows from DB then filters
    // experienceYears / dailyRate / availability in Java memory.
    // This is fine for small datasets (<1000 workers) but will degrade at scale.
    // When you need to scale, replace this with a JPA Specification or a @Query
    // that pushes all predicates into SQL.
    public List<WorkerProfile> filter(String skill,
                                      Integer minExp,
                                      Double maxRate,
                                      String availability) {

        List<WorkerProfile> base = (skill != null && !skill.isBlank())
                ? profileRepository
                .findBySkillCategoryIgnoreCaseContainingAndLocationIgnoreCaseContaining(skill, "")
                : profileRepository.findAll();

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

    // ── CREATE ───────────────────────────────────────────────────────────────

    // ✅ FIX: email passed in from controller — service no longer touches SecurityContextHolder
    @Transactional
    public WorkerProfile create(CreateWorkerProfileRequest req, String email) {

        if (req == null) throw new IllegalArgumentException("Request body is required");

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        profileRepository.findByUser(user).ifPresent(p -> {
            throw new IllegalArgumentException("A worker profile already exists for this account");
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

    // ── READ ─────────────────────────────────────────────────────────────────

    public Optional<WorkerProfile> getById(Long id) {
        return profileRepository.findById(id);
    }

    // ✅ FIX: was fetching AppUser first, then calling findByUser(user).
    //         findByUserId() does the same thing in one query.
    public Optional<WorkerProfile> getByUserId(Long userId) {
        return profileRepository.findByUserId(userId);
    }

    public List<WorkerProfile> listAll() {
        return profileRepository.findAll();
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public WorkerProfile update(Long id, UpdateWorkerProfileRequest req) {
        WorkerProfile p = profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Worker profile not found"));

        if (req.getFullName()       != null) p.setFullName(req.getFullName());
        if (req.getSkillCategory()  != null) p.setSkillCategory(req.getSkillCategory());
        if (req.getExperienceYears()!= null) p.setExperienceYears(req.getExperienceYears());
        if (req.getDailyRate()      != null) p.setDailyRate(req.getDailyRate());
        if (req.getHourlyRate()     != null) p.setHourlyRate(req.getHourlyRate());
        if (req.getLocation()       != null) p.setLocation(req.getLocation());
        if (req.getBio()            != null) p.setBio(req.getBio());
        if (req.getPhone()          != null) p.setPhone(req.getPhone());
        if (req.getAvailability()   != null) p.setAvailability(req.getAvailability());

        return profileRepository.save(p);
    }

    // ── MEDIA ─────────────────────────────────────────────────────────────────

    @Transactional
    public WorkerProfile updateMedia(WorkerProfile profile) {
        return profileRepository.save(profile);
    }
}
