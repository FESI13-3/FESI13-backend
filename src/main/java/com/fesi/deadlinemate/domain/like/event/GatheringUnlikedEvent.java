package com.fesi.deadlinemate.domain.like.event;

public record GatheringUnlikedEvent(
        Long gatheringId,
        Long userId
) {
}
