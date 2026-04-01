package com.fesi.deadlinemate.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.notification.entity.Notification;
import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import com.fesi.deadlinemate.global.config.JpaConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private Long userId = 1L;
    private Long otherUserId = 2L;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        notificationRepository.saveAll(List.of(
                notification(userId, NotificationType.APPLICATION_ACCEPTED, false),
                notification(userId, NotificationType.GATHERING_STARTED, false),
                notification(userId, NotificationType.POKE, true),
                notification(otherUserId, NotificationType.APPLICATION_REJECTED, false)
        ));
    }

    @Nested
    @DisplayName("알림 목록 조회")
    class FindByUserId {

        @Test
        @DisplayName("userId에 해당하는 알림만 조회한다")
        void findByUserId() {
            Page<Notification> result = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10));
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent()).allMatch(n -> n.getUserId().equals(userId));
        }

        @Test
        @DisplayName("페이지 크기만큼만 반환한다")
        void pagination() {
            Page<Notification> result = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 2));
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("읽지 않은 알림 수 조회")
    class CountUnread {

        @Test
        @DisplayName("읽지 않은 알림 수를 반환한다")
        void countUnread() {
            assertThat(notificationRepository.countByUserIdAndIsReadFalse(userId)).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("전체 읽음 처리")
    class MarkAllAsRead {

        @Test
        @DisplayName("해당 유저의 알림을 모두 읽음 처리한다")
        void markAllAsRead() {
            notificationRepository.markAllAsRead(userId);
            List<Notification> notifications = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10)).getContent();
            assertThat(notifications).allMatch(Notification::isRead);
        }

        @Test
        @DisplayName("다른 유저의 알림은 영향받지 않는다")
        void doesNotAffectOtherUsers() {
            notificationRepository.markAllAsRead(userId);
            assertThat(notificationRepository.countByUserIdAndIsReadFalse(otherUserId)).isEqualTo(1L);
        }
    }

    private Notification notification(Long userId, NotificationType type, boolean read) {
        Notification n = Notification.builder()
                .userId(userId).type(type).content("테스트 알림").targetUrl("/test").build();
        if (read) n.markAsRead();
        return n;
    }
}
