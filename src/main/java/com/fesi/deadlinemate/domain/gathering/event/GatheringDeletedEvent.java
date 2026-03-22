package com.fesi.deadlinemate.domain.gathering.event;

public record GatheringDeletedEvent(
        Long gatheringId,
        Long leaderId,
        String title
) {
}
