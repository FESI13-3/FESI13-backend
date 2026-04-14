package com.fesi.deadlinemate.domain.gathering.event;

public record GatheringPokedEvent(
        Long gatheringId,
        Long pokerId,
        Long targetUserId,
        String gatheringTitle
) {
}
