package com.carpool.repository;

import com.carpool.entity.ParcelCarrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParcelCarrierRepository extends JpaRepository<ParcelCarrier, UUID> {
    List<ParcelCarrier> findByParcelId(UUID parcelId);
    List<ParcelCarrier> findByOwnerId(UUID ownerId);
    boolean existsByParcelIdAndOwnerId(UUID parcelId, UUID ownerId);
}
