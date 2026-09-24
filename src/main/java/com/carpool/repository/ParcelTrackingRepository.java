package com.carpool.repository;

import com.carpool.entity.Parcel;
import com.carpool.entity.ParcelTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParcelTrackingRepository extends JpaRepository<ParcelTracking, UUID> {
    List<ParcelTracking> findByParcelOrderByTrackedAtDesc(Parcel parcel);
    List<ParcelTracking> findByParcelIdOrderByTrackedAtDesc(UUID parcelId);
}
