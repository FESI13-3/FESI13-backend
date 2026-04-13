package com.fesi.deadlinemate.domain.gatheringApplication.dto.request;

import com.fesi.deadlinemate.domain.gatheringApplication.command.CreateApplicationCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateApplicationRequest(
        @Schema(example = "매주 빠지지 않고 참여하며 스터디 목표를 달성하겠습니다.")
        @NotBlank(message = "개인 목표는 필수입니다.")
        @Size(max = 1000, message = "개인 목표는 1000자 이하여야 합니다.")
        String personalGoal,

        @Schema(example = "백엔드 개발 2년차이며 React를 처음 배우고 싶어 지원했습니다.")
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
