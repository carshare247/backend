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
@Table(name = "parcel_proofs")
public class ParcelProof extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    @Column(precision = 10, scale = 6)
    private BigDecimal pickupLatitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal pickupLongitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal deliveryLatitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal deliveryLongitude;

    @Column(length = 500)
    private String pickupImageUrl;

    @Column(length = 500)
    private String deliveryImageUrl;

    @Column(length = 500)
    private String senderAcknowledgement;

    @Column(length = 500)
    private String receiverAcknowledgement;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
