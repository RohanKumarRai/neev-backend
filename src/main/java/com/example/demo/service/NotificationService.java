package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// ✅ CHANGES:
//
//  1. SSE emitter timeout changed from 0L (infinite) to 3 minutes (180_000L ms).
//     A timeout of 0L means the server holds the connection open forever, even after
//     the client disconnects without sending a close frame (e.g. browser tab closed,
//     mobile app backgrounded, network drop). This causes dead emitters to accumulate
//     in memory — on a busy server they eventually cause an OOM.
//
//     With a 3-minute timeout the client reconnects automatically (EventSource in the
//     browser reconnects by default). 3 minutes is a balance between real-time feel
//     and connection overhead; adjust via the SSE_TIMEOUT_MS constant if needed.
//
//  2. All other logic (create, markRead, markAllRead, pushToEmitters) is unchanged.

@Service
public class NotificationService {

    // ✅ FIX: 3-minute timeout instead of 0 (infinite).
    //         Browser EventSource / mobile clients will reconnect automatically.
    private static final long SSE_TIMEOUT_MS = 3 * 60 * 1_000L;

    private final NotificationRepository repo;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public Notification create(Long recipientUserId, String type, String message,
                               Long jobId, Long actorUserId) {
        Notification n = new Notification();
        n.setRecipientUserId(recipientUserId);
        n.setType(type);
        n.setMessage(message);
        n.setJobId(jobId);
        n.setActorUserId(actorUserId);
        n.setCreatedAt(Instant.now());
        n.setRead(false);
        Notification saved = repo.save(n);
        pushToEmitters(recipientUserId, saved);
        return saved;
    }

    public List<Notification> getForUser(Long userId) {
        return repo.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> listForUser(Long userId) {
        return getForUser(userId);
    }

    public List<Notification> getUnread(Long userId) {
        return repo.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public long countUnread(Long userId) {
        return repo.countByRecipientUserIdAndReadFalse(userId);
    }

    public Notification markRead(Long id) {
        Notification n = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (n.getRead() == null || !n.getRead()) {
            n.setRead(true);
            n.setReadAt(Instant.now());
            n = repo.save(n);
        }
        return n;
    }

    public int markAllRead(Long userId) {
        List<Notification> unread =
                repo.findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> {
            n.setRead(true);
            n.setReadAt(Instant.now());
        });
        repo.saveAll(unread);
        return unread.size();
    }

    // ── SSE ───────────────────────────────────────────────────────────────────

    public SseEmitter registerEmitter(Long userId) {
        // ✅ FIX: 3-minute timeout — prevents dead emitter accumulation
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitters.computeIfAbsent(userId,
                k -> Collections.synchronizedList(new ArrayList<>())).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(()    -> removeEmitter(userId, emitter));
        emitter.onError((ex)    -> removeEmitter(userId, emitter));

        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) list.remove(emitter);
    }

    private void pushToEmitters(Long userId, Notification n) {
        List<SseEmitter> list = emitters.getOrDefault(userId, Collections.emptyList());
        for (SseEmitter e : new ArrayList<>(list)) {
            try {
                e.send(SseEmitter.event().name("notification").data(n));
            } catch (Exception ex) {
                removeEmitter(userId, e);
            }
        }
    }
}
