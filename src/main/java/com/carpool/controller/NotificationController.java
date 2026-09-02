package com.carpool.controller;

import com.carpool.dto.ApiResponse;
import com.carpool.entity.Role;
import com.carpool.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<?> list(@RequestParam Role role) {
        return ApiResponse.of(notificationService.myNotifications(role));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<?> markRead(@PathVariable UUID notificationId, @RequestParam Role role) {
        notificationService.markRead(notificationId, role);
        return ApiResponse.of(java.util.Map.of("status", "ok"));
    }

    @PatchMapping("/read-all")
    public ApiResponse<?> markAllRead(@RequestParam Role role) {
        notificationService.markAllRead(role);
        return ApiResponse.of(java.util.Map.of("status", "ok"));
    }
}
