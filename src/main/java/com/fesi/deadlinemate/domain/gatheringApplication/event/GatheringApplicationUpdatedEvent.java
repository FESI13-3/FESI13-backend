package com.fesi.deadlinemate.domain.gatheringApplication.event;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;

public record GatheringApplicationUpdatedEvent(
        Long applicationId,
        Long gatheringId,
        Long applicantId,
        Long leaderId,
        String gatheringTitle,
        ApplicationStatus status
) {
}
