package com.carpool.repository;

import com.carpool.entity.CarbonAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarbonAchievementRepository extends JpaRepository<CarbonAchievement, UUID> {
    List<CarbonAchievement> findByUserIdOrderByAwardedAtDesc(UUID userId);
}
