package com.fesi.deadlinemate.domain.gathering.event;

import java.math.BigDecimal;

public record GatheringMemberEvaluatedEvent(
        Long gatheringId,
        Long userId,
        BigDecimal reputationDelta,
        boolean hasPenalty
) {
}
