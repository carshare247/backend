package com.carpool.service;

import com.carpool.entity.*;
import com.carpool.exception.AppException;
import com.carpool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarbonService {

    private final RideRepository rideRepository;
    private final CarbonFootprintRepository carbonFootprintRepository;
    private final CarbonCalculationRuleRepository carbonCalculationRuleRepository;
    private final CarbonAchievementRepository carbonAchievementRepository;
    private final UserRepository userRepository;

    @Transactional
    public CarbonFootprint calculateForRide(UUID rideId) {
        Ride ride = rideRepository.findById(rideId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "RIDE_NOT_FOUND", "Ride not found"));

        CarbonCalculationRule rule = carbonCalculationRuleRepository.findFirstByActiveTrueOrderByCreatedAtDesc()
            .orElseGet(() -> {
                CarbonCalculationRule defaultRule = new CarbonCalculationRule();
                defaultRule.setName("DEFAULT");
                return carbonCalculationRuleRepository.save(defaultRule);
            });

        BigDecimal distanceKm = BigDecimal.valueOf(12.5);
        int passengers = Math.max(1, ride.getTotalSeats() - ride.getAvailableSeats());
        BigDecimal fuelSavedLitres = distanceKm.multiply(rule.getFuelSavedPerPassengerKm()).multiply(BigDecimal.valueOf(passengers));
        BigDecimal passiveEmission = distanceKm.multiply(rule.getEmissionRateKgPerKm()).multiply(BigDecimal.valueOf(passengers));
        BigDecimal co2ReducedKg = passiveEmission.setScale(2, RoundingMode.HALF_UP);
        BigDecimal carbonSaved = co2ReducedKg.setScale(2, RoundingMode.HALF_UP);
        BigDecimal treesEquivalent = carbonSaved.divide(rule.getTreeAbsorptionKgPerYear(), 2, RoundingMode.HALF_UP);
        BigDecimal environmentalScore = BigDecimal.valueOf(75).add(BigDecimal.valueOf(passengers).multiply(BigDecimal.valueOf(5)))
            .setScale(2, RoundingMode.HALF_UP);

        CarbonFootprint footprint = new CarbonFootprint();
        footprint.setRide(ride);
        footprint.setUser(ride.getOwner().getUser());
        footprint.setDistanceKm(distanceKm);
        footprint.setPassengers(passengers);
        footprint.setFuelSavedLitres(fuelSavedLitres);
        footprint.setCo2ReducedKg(co2ReducedKg);
        footprint.setCarbonSaved(carbonSaved);
        footprint.setTreesEquivalent(treesEquivalent);
        footprint.setEnvironmentalScore(environmentalScore);
        footprint.setCalculationSource("DEFAULT");
        footprint = carbonFootprintRepository.save(footprint);

        awardAchievements(ride.getOwner().getUser());
        return footprint;
    }

    @Transactional(readOnly = true)
    public List<CarbonFootprint> myCarbonFootprint(UUID userId) {
        return carbonFootprintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void awardAchievements(User user) {
        List<CarbonAchievement> existing = carbonAchievementRepository.findByUserIdOrderByAwardedAtDesc(user.getId());
        if (existing.size() < 1) {
            CarbonAchievement achievement = new CarbonAchievement();
            achievement.setUser(user);
            achievement.setBadgeName("Green Starter");
            achievement.setDescription("Completed your first shared ride contribution");
            carbonAchievementRepository.save(achievement);
        }
    }
}
