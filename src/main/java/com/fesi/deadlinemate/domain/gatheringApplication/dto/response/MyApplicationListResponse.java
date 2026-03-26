package com.fesi.deadlinemate.domain.gatheringApplication.dto.response;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record MyApplicationListResponse(
        List<MyApplicationItemResponse> applications
) {
    public static MyApplicationListResponse of(List<MyApplicationItemResponse> applications) {
        return MyApplicationListResponse.builder()
                .applications(applications)
                .build();
    }

    @Builder
    public record MyApplicationItemResponse(
            Long id,
            GatheringSummaryResponse gathering,
            String personalGoal,
            ApplicationStatus status,
            LocalDateTime createdAt
    ) {
    }

    @Builder
    public record GatheringSummaryResponse(
            Long id,
            String title,
            String type,
            GatheringStatus status
    ) {
    }
}
