package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    // SseEmitters per userId
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    // Create notification (saves + pushes to SSE if present)
    public Notification create(Long recipientUserId, String type, String message, Long jobId, Long actorUserId) {
        Notification n = new Notification();
        n.setRecipientUserId(recipientUserId);
        n.setType(type);
        n.setMessage(message);
        n.setJobId(jobId);
        n.setActorUserId(actorUserId);
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        Notification saved = repo.save(n);

        // push to SSE subscribers if any
        pushToEmitters(recipientUserId, saved);
        return saved;
    }

    // alias for controller naming
    public List<Notification> listForUser(Long userId) {
        return repo.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getForUser(Long userId) {
        return listForUser(userId);
    }

    public List<Notification> getUnread(Long userId) {
        return repo.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return repo.countByRecipientUserIdAndReadFalse(userId);
    }

    public Notification markRead(Long id) {
        Notification n = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (n.getRead() == null || !n.getRead()) {
            n.setRead(true);
            n.setReadAt(Instant.now());
            n = repo.save(n);
        }
        return n;
    }

    public int markAllRead(Long userId) {
        List<Notification> unread = repo.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
        });
        repo.saveAll(unread);
        return unread.size();
    }

    // ---------- SSE wiring ----------
    public SseEmitter registerEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((ex) -> removeEmitter(userId, emitter));

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    private void pushToEmitters(Long userId, Notification n) {
        List<SseEmitter> list = emitters.getOrDefault(userId, Collections.emptyList());
        for (SseEmitter e : new ArrayList<>(list)) {
            try {
                e.send(SseEmitter.event().name("notification").data(n));
            } catch (Exception ex) {
                // remove broken emitter
                removeEmitter(userId, e);
            }
        }
    }
}
