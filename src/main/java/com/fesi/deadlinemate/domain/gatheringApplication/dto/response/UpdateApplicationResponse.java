package com.fesi.deadlinemate.domain.gatheringApplication.dto.response;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import lombok.Builder;

@Builder
public record UpdateApplicationResponse(
        Long id,
        ApplicationStatus status
) {
    public static UpdateApplicationResponse from(GatheringApplication application) {
        return UpdateApplicationResponse.builder()
                .id(application.getId())
                .status(application.getStatus())
                .build();
    }
}
