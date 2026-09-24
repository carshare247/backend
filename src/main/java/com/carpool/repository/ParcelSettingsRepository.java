package com.carpool.repository;

import com.carpool.entity.ParcelSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParcelSettingsRepository extends JpaRepository<ParcelSettings, UUID> {
    Optional<ParcelSettings> findTopByOrderByCreatedAtAsc();
}
