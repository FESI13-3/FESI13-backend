package com.fesi.deadlinemate.domain.gatheringApplication.dto.request;

import com.fesi.deadlinemate.domain.gatheringApplication.command.CreateApplicationCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateApplicationRequest(
        @NotBlank(message = "개인 목표는 필수입니다.")
        @Size(max = 1000, message = "개인 목표는 1000자 이하여야 합니다.")
        String personalGoal,

        @Size(max = 1000, message = "자기소개는 1000자 이하여야 합니다.")
        String selfIntroduction
) {
    public CreateApplicationCommand toCommand(Long gatheringId, Long applicantId) {
        return CreateApplicationCommand.builder()
                .gatheringId(gatheringId)
                .applicantId(applicantId)
                .personalGoal(personalGoal)
                .selfIntroduction(selfIntroduction)
                .build();
    }
}
