package com.carpool.controller;

import com.carpool.dto.ApiResponse;
import com.carpool.entity.Role;
import com.carpool.exception.AppException;
import com.carpool.security.AuthFacade;
import com.carpool.service.ParcelService;
import com.carpool.service.ParcelSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/parcels")
@RequiredArgsConstructor
public class AdminParcelController {
    private final ParcelService parcelService;
    private final ParcelSettingsService settingsService;
    private final AuthFacade authFacade;

    @GetMapping
    public ApiResponse<?> list() {
        requireAdmin();
        return ApiResponse.of(parcelService.adminParcels());
    }

    @GetMapping("/settings")
    public ApiResponse<?> settings() {
        requireAdmin();
        return ApiResponse.of(Map.of("deliveryFee", settingsService.currentDeliveryFee()));
    }

    @PutMapping("/settings")
    public ApiResponse<?> updateSettings(@RequestBody Map<String, Object> payload) {
        requireAdmin();
        Object rawFee = payload == null ? null : payload.get("deliveryFee");
        try {
            BigDecimal fee = new BigDecimal(String.valueOf(rawFee));
            return ApiResponse.of(Map.of("deliveryFee", settingsService.updateDeliveryFee(fee).getDeliveryFee()));
        } catch (NumberFormatException ex) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_DELIVERY_FEE", "Enter a valid delivery fee");
        }
    }

    private void requireAdmin() {
        if (authFacade.currentUser().getRole() != Role.ADMIN) {
            throw new AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Admin access required");
        }
    }
}
