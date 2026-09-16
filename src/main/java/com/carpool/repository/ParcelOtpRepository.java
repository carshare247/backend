package com.carpool.repository;

import com.carpool.entity.Parcel;
import com.carpool.entity.ParcelOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParcelOtpRepository extends JpaRepository<ParcelOtp, UUID> {
    List<ParcelOtp> findByParcelOrderByCreatedAtDesc(Parcel parcel);
    Optional<ParcelOtp> findTopByParcelIdAndPurposeOrderByCreatedAtDesc(UUID parcelId, String purpose);
}
