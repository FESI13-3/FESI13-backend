package com.fesi.deadlinemate.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fesi.deadlinemate.domain.notification.dto.response.NotificationListResponse;
import com.fesi.deadlinemate.domain.notification.entity.Notification;
import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import com.fesi.deadlinemate.domain.notification.repository.NotificationRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @Test
    @DisplayName("알림 목록과 unreadCount를 반환한다")
    void getNotifications() {
        Notification n1 = notification(1L, 1L, false);
        Notification n2 = notification(2L, 1L, true);

        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(n1, n2)));
        given(notificationRepository.countByUserIdAndIsReadFalse(1L)).willReturn(1L);

        NotificationListResponse response = notificationQueryService.getNotifications(1L, 1, 20);

        assertThat(response.notifications()).hasSize(2);
        assertThat(response.unreadCount()).isEqualTo(1L);
    }

    private Notification notification(Long id, Long userId, boolean isRead) {
        Notification n = Notification.builder()
                .userId(userId).type(NotificationType.APPLICATION_ACCEPTED)
                .content("테스트").targetUrl("/test").build();
        if (isRead) n.markAsRead();
        try {
            Field f = Notification.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(n, id);
        } catch (Exception e) { throw new RuntimeException(e); }
        return n;
    }
}
