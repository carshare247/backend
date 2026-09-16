package com.carpool.repository;

import com.carpool.entity.CarbonCalculationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CarbonCalculationRuleRepository extends JpaRepository<CarbonCalculationRule, UUID> {
    Optional<CarbonCalculationRule> findByNameAndActive(String name, boolean active);
    Optional<CarbonCalculationRule> findFirstByActiveTrueOrderByCreatedAtDesc();
}
