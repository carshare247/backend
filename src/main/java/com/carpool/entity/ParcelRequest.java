package com.carpool.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "parcel_requests")
public class ParcelRequest extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    @Column(length = 200)
    private String pickupAddress;

    @Column(length = 200)
    private String dropAddress;

    @Column(precision = 10, scale = 2)
    private BigDecimal pickupLatitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal pickupLongitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal dropLatitude;

    @Column(precision = 10, scale = 2)
    private BigDecimal dropLongitude;

    @Column(nullable = false)
    private boolean matched = false;
}
