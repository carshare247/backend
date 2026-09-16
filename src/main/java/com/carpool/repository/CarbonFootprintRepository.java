package com.carpool.repository;

import com.carpool.entity.CarbonFootprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarbonFootprintRepository extends JpaRepository<CarbonFootprint, UUID> {
    List<CarbonFootprint> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<CarbonFootprint> findByRideId(UUID rideId);
}
