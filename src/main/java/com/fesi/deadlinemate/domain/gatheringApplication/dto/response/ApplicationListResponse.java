package com.fesi.deadlinemate.domain.gatheringApplication.dto.response;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ApplicationListResponse(
        List<ApplicationItemResponse> applications
) {
    public static ApplicationListResponse of(List<ApplicationItemResponse> applications) {
        return ApplicationListResponse.builder()
                .applications(applications)
                .build();
    }

    @Builder
    public record ApplicationItemResponse(
            Long id,
            ApplicantResponse applicant,
            String personalGoal,
            String selfIntroduction,
            ApplicationStatus status,
            LocalDateTime createdAt
    ) {
    }

    @Builder
    public record ApplicantResponse(
            Long id,
            String nickname,
            String profileImage,
            BigDecimal reputationScore
    ) {
    }
}
