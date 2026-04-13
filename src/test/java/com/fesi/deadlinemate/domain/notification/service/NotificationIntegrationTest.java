package com.fesi.deadlinemate.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.gatheringApplication.command.CreateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.repository.GatheringApplicationRepository;
import com.fesi.deadlinemate.domain.gatheringApplication.service.GatheringApplicationService;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.notification.entity.Notification;
import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import com.fesi.deadlinemate.domain.notification.repository.NotificationRepository;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * 알림 통합 테스트.
 *
 * @Transactional 미사용 — @TransactionalEventListener(AFTER_COMMIT) 은
 * 트랜잭션이 실제로 커밋되어야 발동하므로, 테스트 자체에 트랜잭션을 걸면
 * 롤백 후 커밋이 없어 리스너가 실행되지 않는다.
 * 대신 @AfterEach 에서 직접 DB 정리한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired private GatheringApplicationService gatheringApplicationService;
    @Autowired private UserRepository userRepository;
    @Autowired private GatheringRepository gatheringRepository;
    @Autowired private GatheringApplicationRepository gatheringApplicationRepository;
    @Autowired private GatheringMemberRepository gatheringMemberRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @AfterEach
    void cleanup() {
        notificationRepository.deleteAll();
        gatheringApplicationRepository.deleteAll();
        gatheringMemberRepository.deleteAll();
        gatheringRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("모임 신청 시 모임장에게 APPLICATION_RECEIVED 알림이 DB에 저장된다")
    void applicationCreatesNotificationForLeader() {
        // given
        User leader = userRepository.save(User.builder()
                .email("leader-notif-integ@test.com")
                .passwordHash(passwordEncoder.encode("Test1234!"))
                .nickname("모임장")
                .provider(Provider.EMAIL)
                .build());

        User applicant = userRepository.save(User.builder()
                .email("applicant-notif-integ@test.com")
                .passwordHash(passwordEncoder.encode("Test1234!"))
                .nickname("신청자")
                .provider(Provider.EMAIL)
                .build());

        Gathering gathering = gatheringRepository.save(Gathering.builder()
                .leaderId(leader.getId())
                .type(GatheringType.STUDY)
                .title("알림 통합테스트 스터디")
                .shortDescription("짧은 소개")
                .description("상세 설명")
                .goal("목표")
                .maxMembers(6)
                .currentMembers(1)
                .recruitDeadline(LocalDate.of(2099, 12, 1))
                .startDate(LocalDate.of(2099, 12, 15))
                .endDate(LocalDate.of(2100, 3, 15))
                .totalWeeks(13)
                .status(GatheringStatus.RECRUITING)
                .viewCount(0)
                .build());

        // when: 신청자가 모임 신청 (트랜잭션 커밋 → AFTER_COMMIT 리스너 발동)
        gatheringApplicationService.create(CreateApplicationCommand.builder()
                .gatheringId(gathering.getId())
                .applicantId(applicant.getId())
                .personalGoal("열심히 공부하겠습니다")
                .build());

        // then: 모임장 ID로 알림이 1건 저장되어 있어야 한다
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(leader.getId(), PageRequest.of(0, 10))
                .getContent();

        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getUserId()).isEqualTo(leader.getId());
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.APPLICATION_RECEIVED);
        assertThat(notifications.get(0).getContent()).contains("알림 통합테스트 스터디");
    }
}
