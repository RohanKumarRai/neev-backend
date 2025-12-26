package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;
    private final AppUserRepository userRepo;

    public NotificationController(NotificationService service,
                                  AppUserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    // 🔐 Utility — get logged-in userId from JWT
    private Long currentUserId(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // =====================================================
    // ✅ GET ALL NOTIFICATIONS (LOGGED-IN USER)
    // =====================================================
    @GetMapping("/my")
    public List<Notification> my(Authentication auth) {
        return service.getForUser(currentUserId(auth));
    }

    // =====================================================
    // ✅ GET UNREAD NOTIFICATIONS
    // =====================================================
    @GetMapping("/unread")
    public List<Notification> unread(Authentication auth) {
        return service.getUnread(currentUserId(auth));
    }

    // =====================================================
    // ✅ GET UNREAD COUNT (BADGE)
    // =====================================================
    @GetMapping("/count")
    public Map<String, Long> count(Authentication auth) {
        return Map.of(
                "unreadCount",
                service.countUnread(currentUserId(auth))
        );
    }

    // =====================================================
    // ✅ MARK SINGLE NOTIFICATION AS READ
    // =====================================================
    @PostMapping("/{id}/read")
    public Notification markRead(@PathVariable Long id) {
        return service.markRead(id);
    }

    // =====================================================
    // ✅ MARK ALL AS READ
    // =====================================================
    @PostMapping("/read-all")
    public Map<String, Integer> markAllRead(Authentication auth) {
        int count = service.markAllRead(currentUserId(auth));
        return Map.of("markedRead", count);
    }

    // =====================================================
    // ✅ SUMMARY (UNREAD COUNT + RECENT 10)
    // =====================================================
    @GetMapping("/summary")
    public Map<String, Object> summary(Authentication auth) {
        Long uid = currentUserId(auth);
        return Map.of(
                "unreadCount", service.countUnread(uid),
                "recent", service.getForUser(uid).stream().limit(10).toList()
        );
    }

    // =====================================================
    // ✅ SECURE SSE STREAM (LOGGED-IN USER ONLY)
    // =====================================================
    @GetMapping("/stream")
    public SseEmitter stream(Authentication auth) {
        return service.registerEmitter(currentUserId(auth));
    }
}
