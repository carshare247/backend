package com.carpool.service;

import com.carpool.entity.ParcelSettings;
import com.carpool.exception.AppException;
import com.carpool.repository.ParcelSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ParcelSettingsService {
    private final ParcelSettingsRepository repository;

    @Transactional
    public ParcelSettings getOrCreate() {
        return repository.findTopByOrderByCreatedAtAsc().orElseGet(() -> repository.save(new ParcelSettings()));
    }

    @Transactional(readOnly = true)
    public BigDecimal currentDeliveryFee() {
        return repository.findTopByOrderByCreatedAtAsc()
            .map(ParcelSettings::getDeliveryFee)
            .orElse(BigDecimal.valueOf(49.00));
    }

    @Transactional
    public ParcelSettings updateDeliveryFee(BigDecimal deliveryFee) {
        if (deliveryFee == null || deliveryFee.signum() < 0) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_DELIVERY_FEE", "Delivery fee must be zero or greater");
        }
        ParcelSettings settings = getOrCreate();
        settings.setDeliveryFee(deliveryFee.setScale(2));
        return repository.save(settings);
    }
}
