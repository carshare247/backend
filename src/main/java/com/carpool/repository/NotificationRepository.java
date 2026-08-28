package com.carpool.repository;

import com.carpool.entity.Notification;
import com.carpool.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndTypeAndCreatedAtBetween(UUID userId, NotificationType type, Instant from, Instant to);
}
