package com.fesi.deadlinemate.domain.user.event;

import com.fesi.deadlinemate.domain.gathering.event.GatheringMemberEvaluatedEvent;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserReputationEventListener {

    private final UserClient userClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMemberEvaluated(GatheringMemberEvaluatedEvent event) {
        userClient.addReputationScore(event.userId(), event.reputationDelta());
    }
}
