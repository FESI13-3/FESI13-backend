package com.fesi.deadlinemate.domain.gatheringApplication.event;

public record GatheringApplicationCancelledEvent(
        Long applicationId,
        Long gatheringId,
        Long applicantId,
        Long leaderId,
        String gatheringTitle
) {
}