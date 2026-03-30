package com.fesi.deadlinemate.domain.gatheringApplication.dto.response;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CreateApplicationResponse(
        Long id,
        ApplicationStatus status,
        LocalDateTime createdAt
) {
    public static CreateApplicationResponse from(GatheringApplication application) {
        return CreateApplicationResponse.builder()
                .id(application.getId())
                .status(application.getStatus())
                .createdAt(application.getCreatedAt())
                .build();
    }
}
