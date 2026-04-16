package com.fesi.deadlinemate.domain.notification.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationCreatedEvent;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationUpdatedEvent;
import com.fesi.deadlinemate.domain.notification.command.SendNotificationCommand;
import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import com.fesi.deadlinemate.domain.notification.entity.ReferenceType;
import com.fesi.deadlinemate.domain.notification.service.NotificationCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GatheringApplicationNotificationEventListenerTest {

    @Mock
    private NotificationCommandService notificationCommandService;

    @InjectMocks
    private GatheringApplicationNotificationEventListener listener;

    @Nested
    @DisplayName("모임 신청 생성 이벤트")
    class HandleApplicationCreated {

        @Test
        @DisplayName("신청이 생성되면 모임장에게 APPLICATION_RECEIVED 알림을 발송한다")
        void sendsApplicationReceivedToLeader() {
            GatheringApplicationCreatedEvent event = new GatheringApplicationCreatedEvent(
                    1L, 10L, 200L, 100L, "React 스터디"
            );

            listener.handleApplicationCreated(event);

            ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
            then(notificationCommandService).should().send(captor.capture());

            SendNotificationCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo(100L); // 모임장 ID
            assertThat(command.type()).isEqualTo(NotificationType.APPLICATION_RECEIVED);
            assertThat(command.content()).contains("React 스터디");
            assertThat(command.targetUrl()).isEqualTo("/gatherings/10");
            assertThat(command.referenceId()).isEqualTo(1L);
            assertThat(command.referenceType()).isEqualTo(ReferenceType.MEMBERSHIP);
        }

        @Test
        @DisplayName("알림 수신자는 신청자가 아닌 모임장이다")
        void receiverIsLeaderNotApplicant() {
            Long leaderId = 100L;
            Long applicantId = 200L;

            GatheringApplicationCreatedEvent event = new GatheringApplicationCreatedEvent(
                    1L, 10L, applicantId, leaderId, "스터디"
            );

            listener.handleApplicationCreated(event);

            ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
            then(notificationCommandService).should().send(captor.capture());

            assertThat(captor.getValue().userId()).isEqualTo(leaderId);
            assertThat(captor.getValue().userId()).isNotEqualTo(applicantId);
        }
    }

    @Nested
    @DisplayName("모임 신청 상태 변경 이벤트")
    class HandleApplicationUpdated {

        @Test
        @DisplayName("신청이 수락되면 신청자에게 APPLICATION_ACCEPTED 알림을 발송한다")
        void acceptedSendsNotificationToApplicant() {
            GatheringApplicationUpdatedEvent event = new GatheringApplicationUpdatedEvent(
                    1L, 10L, 200L, 100L, "React 스터디", ApplicationStatus.ACCEPTED
            );

            listener.handleApplicationUpdated(event);

            ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
            then(notificationCommandService).should().send(captor.capture());

            SendNotificationCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo(200L); // 신청자 ID
            assertThat(command.type()).isEqualTo(NotificationType.APPLICATION_ACCEPTED);
            assertThat(command.content()).contains("React 스터디");
            assertThat(command.targetUrl()).isEqualTo("/gatherings/10");
            assertThat(command.referenceId()).isEqualTo(1L);
            assertThat(command.referenceType()).isEqualTo(ReferenceType.MEMBERSHIP);
        }

        @Test
        @DisplayName("신청이 거절되면 신청자에게 APPLICATION_REJECTED 알림을 발송한다")
        void rejectedSendsNotificationToApplicant() {
            GatheringApplicationUpdatedEvent event = new GatheringApplicationUpdatedEvent(
                    1L, 10L, 200L, 100L, "React 스터디", ApplicationStatus.REJECTED
            );

            listener.handleApplicationUpdated(event);

            ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
            then(notificationCommandService).should().send(captor.capture());

            SendNotificationCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo(200L); // 신청자 ID
            assertThat(command.type()).isEqualTo(NotificationType.APPLICATION_REJECTED);
            assertThat(command.content()).contains("React 스터디");
            assertThat(command.targetUrl()).isEqualTo("/gatherings/10");
        }

        @Test
        @DisplayName("수락/거절 어느 경우든 알림 수신자는 신청자이고 모임장이 아니다")
        void receiverIsApplicantNotLeader() {
            Long leaderId = 100L;
            Long applicantId = 200L;

            GatheringApplicationUpdatedEvent event = new GatheringApplicationUpdatedEvent(
                    1L, 10L, applicantId, leaderId, "스터디", ApplicationStatus.ACCEPTED
            );

            listener.handleApplicationUpdated(event);

            ArgumentCaptor<SendNotificationCommand> captor = ArgumentCaptor.forClass(SendNotificationCommand.class);
            then(notificationCommandService).should().send(captor.capture());

            assertThat(captor.getValue().userId()).isEqualTo(applicantId);
            assertThat(captor.getValue().userId()).isNotEqualTo(leaderId);
        }

        @Test
        @DisplayName("PENDING 상태 이벤트는 알림을 발송하지 않는다")
        void pendingDoesNotSendNotification() {
            GatheringApplicationUpdatedEvent event = new GatheringApplicationUpdatedEvent(
                    1L, 10L, 200L, 100L, "React 스터디", ApplicationStatus.PENDING
            );

            listener.handleApplicationUpdated(event);

            then(notificationCommandService).should(never()).send(org.mockito.ArgumentMatchers.any());
        }
    }
}
