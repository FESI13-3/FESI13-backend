package com.fesi.deadlinemate.domain.gatheringApplication.event;

public record GatheringApplicationCreatedEvent(
        Long applicationId,
        Long gatheringId,
        Long applicantId,
        Long leaderId,
        String gatheringTitle
) {
}
