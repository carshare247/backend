package com.carpool.repository;

import com.carpool.entity.Parcel;
import com.carpool.entity.ParcelStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParcelRepository extends JpaRepository<Parcel, UUID> {
    List<Parcel> findBySenderIdOrderByCreatedAtDesc(UUID senderId);
    List<Parcel> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    List<Parcel> findByStatusOrderByCreatedAtDesc(ParcelStatus status);
    List<Parcel> findByRideId(UUID rideId);
    List<Parcel> findAllByOrderByCreatedAtDesc();
}
