package com.fesi.deadlinemate.domain.gatheringApplication.command;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import lombok.Builder;

@Builder
public record UpdateApplicationCommand(
        Long gatheringId,
        Long applicationId,
        Long requesterId,
        ApplicationStatus status
) {
}
