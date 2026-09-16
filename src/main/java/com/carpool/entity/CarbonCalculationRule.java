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
@Table(name = "carbon_calculation_rules")
public class CarbonCalculationRule extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal emissionRateKgPerKm = new BigDecimal("0.120");

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal fuelSavedPerPassengerKm = new BigDecimal("0.025");

    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal treeAbsorptionKgPerYear = new BigDecimal("21.8");

    @Column(nullable = false)
    private boolean active = true;
}
