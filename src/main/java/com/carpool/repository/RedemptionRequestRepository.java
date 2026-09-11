package com.carpool.repository;

import com.carpool.entity.RedemptionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RedemptionRequestRepository extends JpaRepository<RedemptionRequest, UUID> {
    List<RedemptionRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<RedemptionRequest> findAllByOrderByCreatedAtDesc();
}
