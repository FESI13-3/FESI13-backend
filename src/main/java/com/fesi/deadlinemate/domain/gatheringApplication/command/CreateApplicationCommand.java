package com.fesi.deadlinemate.domain.gatheringApplication.command;

import lombok.Builder;

@Builder
public record CreateApplicationCommand(
        Long gatheringId,
        Long applicantId,
        String personalGoal,
        String selfIntroduction
) {
}
