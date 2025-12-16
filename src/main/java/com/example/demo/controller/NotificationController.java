package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // SSE stream for user (keep connection open)
    @GetMapping(value = "/stream/{userId}")
    public SseEmitter stream(@PathVariable Long userId) {
        return service.registerEmitter(userId);
    }

    // List notifications for a user
    @GetMapping("/{userId}")
    public ResponseEntity<List<Notification>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listForUser(userId));
    }

    // Unread list
    @GetMapping("/unread/{userId}")
    public ResponseEntity<?> unread(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUnread(userId));
    }

    // Unread count
    @GetMapping("/count/{userId}")
    public ResponseEntity<?> count(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("unreadCount", service.countUnread(userId)));
    }

    // Mark single read
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable Long id) {
        try {
            Notification updated = service.markRead(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Mark all read for a user
    @PostMapping("/read-all/{userId}")
    public ResponseEntity<?> markAllRead(@PathVariable Long userId) {
        int count = service.markAllRead(userId);
        return ResponseEntity.ok(Map.of("markedRead", count));
    }

    // Summary: unread count + recent 10
    @GetMapping("/summary/{userId}")
    public ResponseEntity<?> summary(@PathVariable Long userId) {
        var list = service.getForUser(userId).stream().limit(10).toList();
        long unread = service.countUnread(userId);
        return ResponseEntity.ok(Map.of("unreadCount", unread, "recent", list));
    }

    // Mark read endpoint kept for compat (if you used earlier)
    @PostMapping("/test")
    public ResponseEntity<?> createTest(@RequestBody Map<String, Object> body) {
        try {
            if (body == null || !body.containsKey("recipientUserId")) {
                return ResponseEntity.badRequest().body("recipientUserId is required");
            }
            Long recipientUserId = Long.valueOf(body.get("recipientUserId").toString());
            String message = body.getOrDefault("message", "Test notification").toString();
            String type = body.getOrDefault("type", "TEST").toString();
            Long jobId = body.get("jobId") == null ? null : Long.valueOf(body.get("jobId").toString());
            Long actorUserId = body.get("actorUserId") == null ? null : Long.valueOf(body.get("actorUserId").toString());

            Notification created = service.create(recipientUserId, type, message, jobId, actorUserId);
            return ResponseEntity.ok(created);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create notification: " + ex.getMessage());
        }
    }
}
