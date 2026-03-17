package com.fesi.deadlinemate.domain.gathering.dto.mock;

import java.time.OffsetDateTime;
import java.util.List;

public class ApplicationDtos {
    public record CreateApplicationRequest(
            String personalGoal,
            String selfIntroduction
    ) {}

    public record UpdateApplicationStatusRequest(
            String status
    ) {}

    public record ApplicantDto(
            Long id,
            String nickname,
            String profileImage,
            Double reputationScore
    ) {}

    public record ApplicationItemDto(
            Long id,
            ApplicantDto applicant,
            String personalGoal,
            String selfIntroduction,
            String status,
            OffsetDateTime createdAt
    ) {}

    public record MyApplicationItemDto(
            Long id,
            Object gathering,
            String personalGoal,
            String status,
            OffsetDateTime createdAt
    ) {}

    public record ApplicationListResponse(
            List<ApplicationItemDto> applications
    ) {}

    public record MyApplicationListResponse(
            List<MyApplicationItemDto> applications
    ) {}

    public static class ApplicationEntity {
        public Long id;
        public Long gatheringId;
        public Long applicantId;
        public String personalGoal;
        public String selfIntroduction;
        public String status;
        public OffsetDateTime createdAt;
    }
}
