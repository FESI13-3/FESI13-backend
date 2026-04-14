package com.fesi.deadlinemate.domain.gathering.event;

import java.util.List;

public record GatheringStartedEvent(
        Long gatheringId,
        Long leaderId,
        String title,
        List<Long> memberUserIds
) {
}
