package com.fesi.deadlinemate.domain.like.event;

public record GatheringLikedEvent(
        Long gatheringId,
        Long userId
) {
}
