package com.carpool.controller;

import com.carpool.dto.ApiResponse;
import com.carpool.security.AuthFacade;
import com.carpool.service.CarbonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/carbon")
@RequiredArgsConstructor
public class CarbonController {

    private final CarbonService carbonService;
    private final AuthFacade authFacade;

    @PostMapping("/rides/{rideId}/calculate")
    public ApiResponse<?> calculate(@PathVariable UUID rideId) {
        return ApiResponse.of(carbonService.calculateForRide(rideId));
    }

    @GetMapping("/me")
    public ApiResponse<?> myFootprint() {
        return ApiResponse.of(carbonService.myCarbonFootprint(authFacade.currentUser().getUserId()));
    }
}
