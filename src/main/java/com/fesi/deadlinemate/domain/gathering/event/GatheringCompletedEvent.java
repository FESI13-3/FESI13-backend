package com.fesi.deadlinemate.domain.gathering.event;

public record GatheringCompletedEvent(
        Long gatheringId,
        Long leaderId,
        String title
) {
}
