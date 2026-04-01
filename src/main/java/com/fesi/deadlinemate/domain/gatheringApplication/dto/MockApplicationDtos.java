package com.fesi.deadlinemate.domain.gatheringApplication.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class MockApplicationDtos {
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
            Map<String, Object> gathering,
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
}
