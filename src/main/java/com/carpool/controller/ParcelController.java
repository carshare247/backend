package com.carpool.controller;

import com.carpool.dto.ApiResponse;
import com.carpool.entity.*;
import com.carpool.security.AuthFacade;
import com.carpool.service.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;
    private final AuthFacade authFacade;

    @PostMapping
    public ApiResponse<?> create(@RequestParam String pickupAddress,
                                @RequestParam String dropAddress,
                                @RequestParam ParcelCategory category,
                                @RequestParam(required = false) UUID rideId,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) BigDecimal weightKg,
                                @RequestParam(required = false) BigDecimal parcelLengthCm,
                                @RequestParam(required = false) BigDecimal parcelWidthCm,
                                @RequestParam(required = false) BigDecimal parcelHeightCm,
                                @RequestParam(required = false) String receiverName,
                                @RequestParam(required = false) String receiverMobile,
                                @RequestParam(required = false) BigDecimal value,
                                @RequestParam(required = false) String specialInstructions,
                                @RequestParam(defaultValue = "false") boolean fragile) {
        return ApiResponse.of(parcelService.createParcel(
            pickupAddress, dropAddress, category, rideId, description, weightKg,
            parcelLengthCm, parcelWidthCm, parcelHeightCm, receiverName,
            receiverMobile, value, specialInstructions, fragile
        ));
    }

    @GetMapping("/me")
    public ApiResponse<?> myParcels() {
        return ApiResponse.of(parcelService.myParcels());
    }

    @GetMapping("/matches")
    public ApiResponse<?> matchingParcels() {
        UUID ownerId = authFacade.currentUser().getOwnerId();
        return ApiResponse.of(parcelService.matchingParcelsForOwner(ownerId));
    }

    @GetMapping("/owner")
    public ApiResponse<?> ownerParcels() {
        return ApiResponse.of(parcelService.ownerParcels());
    }

    @GetMapping("/{parcelId}/track")
    public ApiResponse<?> track(@PathVariable UUID parcelId) {
        return ApiResponse.of(parcelService.trackParcel(parcelId));
    }

    @PostMapping("/{parcelId}/accept")
    public ApiResponse<?> accept(@PathVariable UUID parcelId, @RequestParam(required = false) UUID rideId) {
        return ApiResponse.of(parcelService.acceptParcel(parcelId, rideId));
    }

    @PostMapping("/{parcelId}/reject")
    public ApiResponse<?> reject(@PathVariable UUID parcelId, @RequestParam(required = false) String reason) {
        return ApiResponse.of(parcelService.rejectParcel(parcelId, reason));
    }

    @PostMapping("/{parcelId}/pickup-otp")
    public ApiResponse<?> generatePickupOtp(@PathVariable UUID parcelId) {
        return ApiResponse.of(parcelService.generatePickupOtp(parcelId));
    }

    @PostMapping("/{parcelId}/pickup-otp/verify")
    public ApiResponse<?> verifyPickupOtp(@PathVariable UUID parcelId, @RequestParam String code) {
        return ApiResponse.of(parcelService.verifyPickupOtp(parcelId, code));
    }

    @PostMapping("/{parcelId}/delivery-otp")
    public ApiResponse<?> generateDeliveryOtp(@PathVariable UUID parcelId) {
        return ApiResponse.of(parcelService.generateDeliveryOtp(parcelId));
    }

    @PostMapping("/{parcelId}/delivery-otp/verify")
    public ApiResponse<?> verifyDeliveryOtp(@PathVariable UUID parcelId, @RequestParam String code) {
        return ApiResponse.of(parcelService.verifyDeliveryOtp(parcelId, code));
    }
}
