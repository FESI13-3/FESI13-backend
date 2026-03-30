package com.fesi.deadlinemate.domain.notification.controller;

import com.fesi.deadlinemate.domain.notification.dto.response.NotificationListResponse;
import com.fesi.deadlinemate.domain.notification.service.NotificationCommandService;
import com.fesi.deadlinemate.domain.notification.service.NotificationQueryService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ApiResponse<NotificationListResponse> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(notificationQueryService.getNotifications(userId, page, limit));
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        notificationCommandService.markAsRead(notificationId, userId);
        return ApiResponse.success();
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationCommandService.markAllAsRead(userId);
        return ApiResponse.success();
    }
}
