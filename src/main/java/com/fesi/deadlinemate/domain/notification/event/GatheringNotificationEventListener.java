package com.fesi.deadlinemate.domain.notification.event;

import com.fesi.deadlinemate.domain.gathering.event.GatheringCompletedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringMemberEvaluatedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringPokedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringStartedEvent;
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
public class GatheringNotificationEventListener {

    private final NotificationCommandService notificationCommandService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGatheringStarted(GatheringStartedEvent event) {
        String targetUrl = "/gatherings/" + event.gatheringId() + "/dashboard";
        String content = "'" + event.title() + "' 모임이 시작되었습니다.";

        event.memberUserIds().forEach(userId ->
                notificationCommandService.send(new SendNotificationCommand(
                        userId,
                        NotificationType.GATHERING_STARTED,
                        content,
                        targetUrl,
                        event.gatheringId(),
                        ReferenceType.GATHERING
                ))
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGatheringCompleted(GatheringCompletedEvent event) {
        String dashboardUrl = "/gatherings/" + event.gatheringId() + "/dashboard";
        String membersUrl = "/gatherings/" + event.gatheringId() + "/dashboard?tab=members";
        String endedContent = "'" + event.title() + "' 모임이 종료되었습니다.";
        String reviewContent = "'" + event.title() + "' 모임이 종료되었습니다. 팀원들에게 리뷰를 남겨보세요.";

        event.memberUserIds().forEach(userId -> {
            notificationCommandService.send(new SendNotificationCommand(
                    userId,
                    NotificationType.GATHERING_ENDED,
                    endedContent,
                    dashboardUrl,
                    event.gatheringId(),
                    ReferenceType.GATHERING
            ));
            notificationCommandService.send(new SendNotificationCommand(
                    userId,
                    NotificationType.REVIEW_REQUEST,
                    reviewContent,
                    membersUrl,
                    event.gatheringId(),
                    ReferenceType.GATHERING
            ));
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberEvaluated(GatheringMemberEvaluatedEvent event) {
        if (!event.hasWeeklyPenalty() && !event.hasConsecutivePenalty()) {
            return;
        }

        String targetUrl;
        String content;
        if (event.hasConsecutivePenalty()) {
            targetUrl = "/my";
            content = "연속으로 주간 목표를 달성하지 못해 평판 점수가 조정되었습니다. 마이페이지에서 확인해 보세요.";
        } else {
            targetUrl = "/gatherings/" + event.gatheringId() + "/dashboard?tab=todos";
            content = "주간 달성률이 50% 미만이어서 평판 점수가 조정되었습니다. 할 일을 확인해 보세요.";
        }

        notificationCommandService.send(new SendNotificationCommand(
                event.userId(),
                NotificationType.PENALTY_WARNING,
                content,
                targetUrl,
                event.gatheringId(),
                ReferenceType.GATHERING
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePoked(GatheringPokedEvent event) {
        String targetUrl = "/gatherings/" + event.gatheringId() + "/dashboard?tab=members";

        notificationCommandService.send(new SendNotificationCommand(
                event.targetUserId(),
                NotificationType.POKE,
                "팀원이 나를 콕 찔렀어요. 나도 찌르러 가기",
                targetUrl,
                event.gatheringId(),
                ReferenceType.GATHERING
        ));
    }
}
