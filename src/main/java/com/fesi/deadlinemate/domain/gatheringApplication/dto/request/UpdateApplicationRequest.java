package com.fesi.deadlinemate.domain.gatheringApplication.dto.request;

import com.fesi.deadlinemate.domain.gatheringApplication.command.UpdateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateApplicationRequest(
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
