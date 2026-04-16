package com.fesi.deadlinemate.domain.notification.event;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationCreatedEvent;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationUpdatedEvent;
import com.fesi.deadlinemate.domain.notification.command.SendNotificationCommand;
import com.fesi.deadlinemate.domain.notification.entity.NotificationType;
import com.fesi.deadlinemate.domain.notification.entity.ReferenceType;
import com.fesi.deadlinemate.domain.notification.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GatheringApplicationNotificationEventListener {

    private final NotificationCommandService notificationCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApplicationCreated(GatheringApplicationCreatedEvent event) {
        notificationCommandService.send(new SendNotificationCommand(
                event.leaderId(),
                NotificationType.APPLICATION_RECEIVED,
                "'" + event.gatheringTitle() + "' 모임에 새로운 신청이 도착했습니다.",
                "/gatherings/" + event.gatheringId(),
                event.applicationId(),
                ReferenceType.MEMBERSHIP
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleApplicationUpdated(GatheringApplicationUpdatedEvent event) {
        if (event.status() == ApplicationStatus.ACCEPTED) {
            notificationCommandService.send(new SendNotificationCommand(
                    event.applicantId(),
                    NotificationType.APPLICATION_ACCEPTED,
                    "'" + event.gatheringTitle() + "' 모임 신청이 수락되었습니다.",
                    "/gatherings/" + event.gatheringId(),
                    event.applicationId(),
                    ReferenceType.MEMBERSHIP
            ));
        } else if (event.status() == ApplicationStatus.REJECTED) {
            notificationCommandService.send(new SendNotificationCommand(
                    event.applicantId(),
                    NotificationType.APPLICATION_REJECTED,
                    "'" + event.gatheringTitle() + "' 모임 신청이 거절되었습니다.",
                    "/gatherings/" + event.gatheringId(),
                    event.applicationId(),
                    ReferenceType.MEMBERSHIP
            ));
        }
    }
}
