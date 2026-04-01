package com.fesi.deadlinemate.domain.notification.command;

import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import com.fesi.deadlinemate.domain.notification.entity.ReferenceType;

public record SendNotificationCommand(
        Long userId,
        NotificationType type,
        String content,
        String targetUrl,
        Long referenceId,
        ReferenceType referenceType
) {}
