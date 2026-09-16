package com.carpool.service;

import com.carpool.entity.*;
import com.carpool.dto.parcel.ParcelResponse;
import com.carpool.exception.AppException;
import com.carpool.repository.*;
import com.carpool.security.AppUserPrincipal;
import com.carpool.security.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private final ParcelRepository parcelRepository;
    private final ParcelCarrierRepository parcelCarrierRepository;
    private final ParcelTrackingRepository parcelTrackingRepository;
    private final ParcelOtpRepository parcelOtpRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final ParcelSettingsService parcelSettingsService;
    private final NotificationService notificationService;
    private final AuthFacade authFacade;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ParcelResponse createParcel(String pickupAddress, String dropAddress, ParcelCategory category, UUID rideId,
                              String description, BigDecimal weightKg, BigDecimal parcelLengthCm,
                              BigDecimal parcelWidthCm, BigDecimal parcelHeightCm, String receiverName,
                              String receiverMobile, BigDecimal value, String specialInstructions,
                              boolean fragile) {
        AppUserPrincipal principal = authFacade.currentUser();
        User sender = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Sender not found"));

        Parcel parcel = new Parcel();
        parcel.setSender(sender);
        parcel.setPickupAddress(pickupAddress);
        parcel.setDropAddress(dropAddress);
        parcel.setCategory(category == null ? ParcelCategory.OTHER : category);
        parcel.setDescription(description);
        parcel.setWeightKg(weightKg);
        parcel.setParcelLengthCm(parcelLengthCm);
        parcel.setParcelWidthCm(parcelWidthCm);
        parcel.setParcelHeightCm(parcelHeightCm);
        parcel.setReceiverName(receiverName);
        parcel.setReceiverMobile(receiverMobile);
        parcel.setValue(value);
        parcel.setSpecialInstructions(specialInstructions);
        parcel.setFragile(fragile);
        parcel.setDeliveryFee(parcelSettingsService.currentDeliveryFee());
        parcel.setStatus(ParcelStatus.REQUESTED);
        Ride selectedRide = rideId == null ? null : rideRepository.findById(rideId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "RIDE_NOT_FOUND", "Selected ride not found"));
        if (selectedRide != null) {
            if (!selectedRide.isAcceptParcel() || !selectedRide.getDate().isAfter(java.time.LocalDate.now().minusDays(1))) {
                throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, "PARCEL_RIDE_UNAVAILABLE", "This ride is not accepting parcel requests");
            }
            parcel.setRide(selectedRide);
        }
        parcel = parcelRepository.save(parcel);

        notificationService.create(sender.getId(), NotificationType.RIDE_CREATED,
            "Parcel created", "Your parcel request has been created and is being matched with eligible rides.");
        var recipientOwners = selectedRide != null ? List.of(selectedRide.getOwner()) : ownerProfileRepository.findAll().stream()
            .filter(OwnerProfile::isVerified).toList();
        recipientOwners.stream()
            .map(OwnerProfile::getUser)
            .filter(ownerUser -> !ownerUser.getId().equals(sender.getId()))
            .forEach(ownerUser -> notificationService.create(
                ownerUser.getId(),
                NotificationType.PARCEL_REQUESTED,
                "New parcel request",
                pickupAddress + " to " + dropAddress + " is waiting for a carrier.",
                "/owner/parcel-dashboard"
            ));
        return toResponse(parcel);
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> myParcels() {
        AppUserPrincipal principal = authFacade.currentUser();
        return parcelRepository.findBySenderIdOrderByCreatedAtDesc(principal.getUserId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> matchingParcelsForOwner(UUID ownerId) {
        ownerId = resolveOwnerId(ownerId);
        OwnerProfile owner = ownerProfileRepository.findById(ownerId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "OWNER_NOT_FOUND", "Owner not found"));
        return parcelRepository.findByStatusOrderByCreatedAtDesc(ParcelStatus.REQUESTED).stream()
            .filter(parcel -> parcel.getRide() == null || parcel.getRide().getOwner().getId().equals(owner.getId()))
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> ownerParcels() {
        UUID ownerId = resolveOwnerId(authFacade.currentUser().getOwnerId());
        return parcelRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ParcelResponse> adminParcels() {
        return parcelRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    private UUID resolveOwnerId(UUID ownerId) {
        if (ownerId != null) return ownerId;
        return ownerProfileRepository.findByUserId(authFacade.currentUser().getUserId())
            .map(OwnerProfile::getId)
            .orElseThrow(() -> new AppException(HttpStatus.FORBIDDEN, "OWNER_REQUIRED", "Owner profile required"));
    }

    @Transactional
    public ParcelResponse acceptParcel(UUID parcelId, UUID rideId) {
        AppUserPrincipal principal = authFacade.currentUser();
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        UUID selectedRideId = rideId != null ? rideId : parcel.getRide() == null ? null : parcel.getRide().getId();
        if (selectedRideId == null) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, "RIDE_REQUIRED", "Select a ride before accepting this parcel");
        }
        Ride ride = rideRepository.findById(selectedRideId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "RIDE_NOT_FOUND", "Ride not found"));

        if (principal.getOwnerId() == null || !principal.getOwnerId().equals(ride.getOwner().getId())) {
            throw new AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the ride owner can accept a parcel request");
        }

        parcel.setOwner(ride.getOwner());
        parcel.setRide(ride);
        parcel.setStatus(ParcelStatus.ACCEPTED);
        parcel.setEstimatedDeliveryMinutes(90);
        parcel = parcelRepository.save(parcel);

        ParcelCarrier carrier = new ParcelCarrier();
        carrier.setParcel(parcel);
        carrier.setRide(ride);
        carrier.setOwner(ride.getOwner());
        carrier.setAccepted(true);
        carrier.setAcceptedAt(Instant.now());
        parcelCarrierRepository.save(carrier);

        notificationService.create(parcel.getSender().getId(), NotificationType.RIDE_CREATED,
            "Carrier assigned", "A carrier accepted your parcel request for the selected route.");
        return toResponse(parcel);
    }

    @Transactional
    public ParcelResponse rejectParcel(UUID parcelId, String reason) {
        AppUserPrincipal principal = authFacade.currentUser();
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));

        if (parcel.getOwner() != null && (principal.getOwnerId() == null || !principal.getOwnerId().equals(parcel.getOwner().getId()))) {
            throw new AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the assigned owner can reject this parcel");
        }

        parcel.setStatus(ParcelStatus.REJECTED);
        parcel.setRejectionReason(reason);
        parcel.setActive(false);
        return toResponse(parcelRepository.save(parcel));
    }

    private ParcelResponse toResponse(Parcel parcel) {
        OwnerProfile owner = parcel.getOwner();
        Ride ride = parcel.getRide();
        return ParcelResponse.builder()
            .id(parcel.getId())
            .rideId(ride == null ? null : ride.getId())
            .ownerId(owner == null ? null : owner.getId())
            .ownerName(owner == null ? null : owner.getName())
            .pickupAddress(parcel.getPickupAddress())
            .dropAddress(parcel.getDropAddress())
            .category(parcel.getCategory())
            .description(parcel.getDescription())
            .weightKg(parcel.getWeightKg())
            .parcelLengthCm(parcel.getParcelLengthCm())
            .parcelWidthCm(parcel.getParcelWidthCm())
            .parcelHeightCm(parcel.getParcelHeightCm())
            .receiverName(parcel.getReceiverName())
            .receiverMobile(parcel.getReceiverMobile())
            .value(parcel.getValue())
            .parcelImageUrl(parcel.getParcelImageUrl())
            .specialInstructions(parcel.getSpecialInstructions())
            .deliveryFee(parcel.getDeliveryFee())
            .status(parcel.getStatus())
            .estimatedDeliveryMinutes(parcel.getEstimatedDeliveryMinutes())
            .fragile(parcel.isFragile())
            .active(parcel.isActive())
            .rejectionReason(parcel.getRejectionReason())
            .createdAt(parcel.getCreatedAt())
            .updatedAt(parcel.getUpdatedAt())
            .build();
    }

    @Transactional
    public String generatePickupOtp(UUID parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        String code = String.format("%06d", secureRandom.nextInt(1000000));
        ParcelOtp otp = new ParcelOtp();
        otp.setParcel(parcel);
        otp.setCode(code);
        otp.setPurpose("PICKUP");
        otp.setExpiresAt(Instant.now().plusSeconds(600));
        otp.setAttempts(0);
        parcelOtpRepository.save(otp);
        parcel.setPickupOtpGeneratedAt(Instant.now());
        parcel.setStatus(ParcelStatus.PICKUP_OTP_GENERATED);
        parcelRepository.save(parcel);
        notificationService.create(parcel.getSender().getId(), NotificationType.PARCEL_OTP_GENERATED,
            "Pickup OTP ready", "Your pickup OTP is " + code + ". Share it with the carrier when handing over the parcel.",
            "/parcel/tracking/" + parcelId);
        return code;
    }

    @Transactional
    public boolean verifyPickupOtp(UUID parcelId, String otpCode) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        ParcelOtp otp = parcelOtpRepository.findTopByParcelIdAndPurposeOrderByCreatedAtDesc(parcelId, "PICKUP")
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "OTP_NOT_FOUND", "Pickup OTP not found"));
        if (otp.isUsed() || Instant.now().isAfter(otp.getExpiresAt())) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, "OTP_EXPIRED", "Pickup OTP expired");
        }
        if (!otp.getCode().equals(otpCode)) {
            otp.setAttempts(otp.getAttempts() + 1);
            parcelOtpRepository.save(otp);
            return false;
        }
        otp.setUsed(true);
        parcelOtpRepository.save(otp);
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        parcelRepository.save(parcel);
        return true;
    }

    @Transactional
    public String generateDeliveryOtp(UUID parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        String code = String.format("%06d", secureRandom.nextInt(1000000));
        ParcelOtp otp = new ParcelOtp();
        otp.setParcel(parcel);
        otp.setCode(code);
        otp.setPurpose("DELIVERY");
        otp.setExpiresAt(Instant.now().plusSeconds(600));
        otp.setAttempts(0);
        parcelOtpRepository.save(otp);
        parcel.setDeliveryOtpGeneratedAt(Instant.now());
        parcel.setStatus(ParcelStatus.DELIVERY_OTP_GENERATED);
        parcelRepository.save(parcel);
        notificationService.create(parcel.getSender().getId(), NotificationType.PARCEL_OTP_GENERATED,
            "Delivery OTP ready", "Your delivery OTP is " + code + ". Share it with the receiver to complete delivery.",
            "/parcel/tracking/" + parcelId);
        return code;
    }

    @Transactional
    public boolean verifyDeliveryOtp(UUID parcelId, String otpCode) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        ParcelOtp otp = parcelOtpRepository.findTopByParcelIdAndPurposeOrderByCreatedAtDesc(parcelId, "DELIVERY")
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "OTP_NOT_FOUND", "Delivery OTP not found"));
        if (otp.isUsed() || Instant.now().isAfter(otp.getExpiresAt())) {
            throw new AppException(HttpStatus.UNPROCESSABLE_ENTITY, "OTP_EXPIRED", "Delivery OTP expired");
        }
        if (!otp.getCode().equals(otpCode)) {
            otp.setAttempts(otp.getAttempts() + 1);
            parcelOtpRepository.save(otp);
            return false;
        }
        otp.setUsed(true);
        parcelOtpRepository.save(otp);
        parcel.setStatus(ParcelStatus.DELIVERED);
        parcelRepository.save(parcel);
        return true;
    }

    @Transactional
    public List<ParcelTracking> trackParcel(UUID parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        List<ParcelTracking> events = parcelTrackingRepository.findByParcelIdOrderByTrackedAtDesc(parcelId);
        if (events.isEmpty()) {
            ParcelTracking initial = new ParcelTracking();
            initial.setParcel(parcel);
            initial.setStatus(parcel.getStatus());
            initial.setMessage("Parcel created");
            initial.setTrackedAt(Instant.now());
            events = List.of(parcelTrackingRepository.save(initial));
        }
        if (events.get(0).getStatus() != parcel.getStatus()) {
            ParcelTracking current = new ParcelTracking();
            current.setStatus(parcel.getStatus());
            current.setMessage("Current parcel status");
            current.setTrackedAt(Instant.now());
            events = new java.util.ArrayList<>(events);
            events.add(0, current);
        }
        return events;
    }

    @Transactional
    public ParcelTracking addTrackingEvent(UUID parcelId, ParcelStatus status, String message, BigDecimal latitude, BigDecimal longitude) {
        Parcel parcel = parcelRepository.findById(parcelId)
            .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PARCEL_NOT_FOUND", "Parcel not found"));
        ParcelTracking tracking = new ParcelTracking();
        tracking.setParcel(parcel);
        tracking.setStatus(status);
        tracking.setMessage(message);
        tracking.setLatitude(latitude);
        tracking.setLongitude(longitude);
        tracking.setTrackedAt(Instant.now());
        parcel.setStatus(status);
        parcelRepository.save(parcel);
        return parcelTrackingRepository.save(tracking);
    }
}
