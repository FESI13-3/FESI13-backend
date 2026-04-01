package com.fesi.deadlinemate.domain.notification.service;

import com.fesi.deadlinemate.domain.notification.dto.response.NotificationListResponse;
import com.fesi.deadlinemate.domain.notification.entity.Notification;
import com.fesi.deadlinemate.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationListResponse getNotifications(Long userId, int page, int limit) {
        int validatedPage = Math.max(page, 1);
        int validatedLimit = Math.max(limit, 1);

        Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, PageRequest.of(validatedPage - 1, validatedLimit)
        );

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return NotificationListResponse.of(result.getContent(), unreadCount);
    }
}
