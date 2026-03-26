package com.fesi.deadlinemate.domain.gathering.event;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;

public record GatheringUpdatedEvent(
        Long gatheringId,
        Long leaderId,
        GatheringStatus status
) {
}
