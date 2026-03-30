package com.fesi.deadlinemate.domain.notification.dto.response;

import com.fesi.deadlinemate.domain.notification.entity.Notification;
import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record NotificationListResponse(
        List<NotificationItem> notifications,
        long unreadCount
) {
    @Builder
    public record NotificationItem(
            Long id,
            NotificationType type,
            String content,
            boolean isRead,
            String targetUrl,
            LocalDateTime createdAt
    ) {
        public static NotificationItem from(Notification notification) {
            return NotificationItem.builder()
                    .id(notification.getId())
                    .type(notification.getType())
                    .content(notification.getContent())
                    .isRead(notification.isRead())
                    .targetUrl(notification.getTargetUrl())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    public static NotificationListResponse of(List<Notification> notifications, long unreadCount) {
        return NotificationListResponse.builder()
                .notifications(notifications.stream().map(NotificationItem::from).toList())
                .unreadCount(unreadCount)
                .build();
    }
}
