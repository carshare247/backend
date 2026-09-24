package com.carpool.dto.parcel;

import com.carpool.entity.ParcelCategory;
import com.carpool.entity.ParcelStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ParcelResponse {
    private UUID id;
    private UUID rideId;
    private UUID ownerId;
    private String ownerName;
    private String pickupAddress;
    private String dropAddress;
    private ParcelCategory category;
    private String description;
    private BigDecimal weightKg;
    private BigDecimal parcelLengthCm;
    private BigDecimal parcelWidthCm;
    private BigDecimal parcelHeightCm;
    private String receiverName;
    private String receiverMobile;
    private BigDecimal value;
    private String parcelImageUrl;
    private String specialInstructions;
    private BigDecimal deliveryFee;
    private ParcelStatus status;
    private Integer estimatedDeliveryMinutes;
    private boolean fragile;
    private boolean active;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;
}