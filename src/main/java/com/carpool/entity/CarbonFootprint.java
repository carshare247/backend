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
@Table(name = "carbon_footprints")
public class CarbonFootprint extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id")
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(precision = 10, scale = 2)
    private BigDecimal distanceKm = BigDecimal.ZERO;

    @Column(nullable = false)
    private int passengers = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal fuelSavedLitres = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal co2ReducedKg = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal carbonSaved = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal treesEquivalent = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal environmentalScore = BigDecimal.ZERO;

    @Column(length = 100)
    private String calculationSource = "DEFAULT";
}
