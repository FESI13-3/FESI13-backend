package com.fesi.deadlinemate.domain.gatheringApplication.dto.request;

import com.fesi.deadlinemate.domain.gatheringApplication.command.UpdateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateApplicationRequest(
        @Schema(example = "ACCEPTED", description = "신청 상태 (ACCEPTED | REJECTED)")
        @NotNull(message = "신청 상태는 필수입니다.")
        ApplicationStatus status
) {
    public UpdateApplicationCommand toCommand(Long gatheringId, Long applicationId, Long requesterId) {
        return UpdateApplicationCommand.builder()
                .gatheringId(gatheringId)
                .applicationId(applicationId)
                .requesterId(requesterId)
                .status(status)
                .build();
    }
}
