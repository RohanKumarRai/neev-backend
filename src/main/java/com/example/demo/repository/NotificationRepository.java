package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByRecipientUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    long countByRecipientUserIdAndReadFalse(Long userId);
}
