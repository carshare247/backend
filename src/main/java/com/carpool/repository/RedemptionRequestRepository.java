package com.carpool.repository;

import com.carpool.entity.RedemptionRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RedemptionRequestRepository extends JpaRepository<RedemptionRequest, UUID> {
    List<RedemptionRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);
    @EntityGraph(attributePaths = "user")
    List<RedemptionRequest> findAllByOrderByCreatedAtDesc();
    @EntityGraph(attributePaths = "user")
    Optional<RedemptionRequest> findByIdWithUser(UUID id);
}
