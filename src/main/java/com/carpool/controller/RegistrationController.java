package com.carpool.controller;

import com.carpool.dto.ApiResponse;
import com.carpool.dto.registration.RegistrationRequest;
import com.carpool.entity.User;
import com.carpool.repository.UserRepository;
import com.carpool.security.AuthFacade;
import com.carpool.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;
    private final AuthFacade authFacade;

    @GetMapping("/resume")
    public ApiResponse<Map<String, Object>> resume() {
        User user = userRepository.findById(authFacade.currentUser().getUserId())
            .orElseThrow(() -> new com.carpool.exception.AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));

        return ApiResponse.of(Map.of(
            "userType", user.getRegistrationType() == null ? null : user.getRegistrationType().name(),
            "stage", registrationService.currentStage(user).name(),
            "registrationCompleted", user.isRegistrationCompleted(),
            "mobileVerified", user.isMobileVerified(),
            "diditStatus", user.getDiditStatus() == null ? null : user.getDiditStatus().name(),
            "profilePhotoUrl", user.getProfilePhotoUrl(),
            "subscriptionPlanId", user.getSubscriptionPlanId(),
            "paymentUTR", user.getPaymentUTR()
        ));
    }

    @PostMapping("/basic")
    public ApiResponse<Map<String, Object>> saveBasicDetails(@Valid @RequestBody RegistrationRequest request) {
        User user = registrationService.upsertRegistrationProfile(request);
        return ApiResponse.of(Map.of("stage", user.getRegistrationStage().name(), "userType", user.getRegistrationType().name()));
    }

    @PostMapping("/otp/verified")
    public ApiResponse<Map<String, Object>> markOtpVerified(@RequestParam String firebaseUid) {
        User user = registrationService.markOtpVerified(firebaseUid);
        return ApiResponse.of(Map.of("stage", user.getRegistrationStage().name(), "mobileVerified", user.isMobileVerified()));
    }

    @PostMapping("/didit/status")
    public ApiResponse<Map<String, Object>> markDiditStatus(@RequestParam String status, @RequestParam(required = false) String sessionId) {
        User user = registrationService.markDiditStatus(status, sessionId);
        return ApiResponse.of(Map.of("stage", user.getRegistrationStage().name(), "diditStatus", user.getDiditStatus().name()));
    }

    @PostMapping("/profile-photo")
    public ApiResponse<Map<String, Object>> markProfilePhoto(@RequestParam String profilePhotoUrl) {
        User user = registrationService.markProfilePhoto(profilePhotoUrl);
        return ApiResponse.of(Map.of("stage", user.getRegistrationStage().name(), "profilePhotoUrl", user.getProfilePhotoUrl()));
    }

    @PostMapping("/subscription")
    public ApiResponse<Map<String, Object>> selectSubscription(@RequestParam String planId) {
        User user = registrationService.selectSubscriptionPlan(planId);
        return ApiResponse.of(Map.of("stage", user.getRegistrationStage().name(), "subscriptionPlanId", user.getSubscriptionPlanId()));
    }

    @PostMapping("/payment")
    public ApiResponse<Map<String, Object>> submitPayment(@RequestParam String utrNumber, @RequestParam String screenshotUrl) {
        User user = registrationService.submitPayment(utrNumber, screenshotUrl);
        return ApiResponse.of(Map.of("stage", user.getRegistrationStage().name(), "registrationCompleted", user.isRegistrationCompleted()));
    }
}
