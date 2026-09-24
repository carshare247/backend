package com.carpool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "parcels")
public class Parcel extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private OwnerProfile owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id")
    private Ride ride;

    @Column(nullable = false, length = 200)
    private String pickupAddress;

    @Column(nullable = false, length = 200)
    private String dropAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParcelCategory category = ParcelCategory.OTHER;

    @Column(length = 1000)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(precision = 10, scale = 2)
    private BigDecimal parcelLengthCm;

    @Column(precision = 10, scale = 2)
    private BigDecimal parcelWidthCm;

    @Column(precision = 10, scale = 2)
    private BigDecimal parcelHeightCm;

    @Column(length = 50)
    private String receiverName;

    @Column(length = 20)
    private String receiverMobile;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(length = 500)
    private String parcelImageUrl;

    @Column(length = 1000)
    private String specialInstructions;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParcelStatus status = ParcelStatus.REQUESTED;

    @Column
    private Integer estimatedDeliveryMinutes;

    @Column
    private Instant pickupOtpGeneratedAt;

    @Column
    private Instant deliveryOtpGeneratedAt;

    @Column(nullable = false)
    private boolean fragile = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String rejectionReason;

    @Column(length = 200)
    private String matchSummary;
}
