package com.fesi.deadlinemate.domain.notification.command;

import com.fesi.deadlinemate.domain.notification.entity.NotificationType;

public record SendNotificationCommand(
        Long userId,
        NotificationType type,
        String content,
        String targetUrl,
        Long referenceId,
        String referenceType
) {}
