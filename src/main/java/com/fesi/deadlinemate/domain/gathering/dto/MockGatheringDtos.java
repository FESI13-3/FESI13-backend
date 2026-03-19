package com.fesi.deadlinemate.domain.gathering.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockGatheringDtos {
    public record LeaderDto(
            Long id,
            String nickname,
            String profileImage
    ) {}

    public record MemberDto(
            Long userId,
            String nickname,
            String profileImage,
            String role,
            Double overallAchievementRate,
            Boolean isActive
    ) {}

    public record WeeklyPlanDto(
            Integer week,
            String title,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record GatheringSummaryDto(
            Long id,
            String type,
            String category,
            String title,
            String shortDescription,
            List<String> tags,
            Integer maxMembers,
            Integer currentMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            String status,
            Boolean isLiked,
            LeaderDto leader
    ) {}

    public record GatheringDetailDto(
            Long id,
            String type,
            String category,
            String title,
            String shortDescription,
            String description,
            List<String> tags,
            String goal,
            Integer maxMembers,
            Integer currentMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            Integer totalWeeks,
            List<Map<String, Object>> images,
            String status,
            Boolean isLiked,
            LeaderDto leader,
            List<WeeklyPlanDto> weeklyPlans,
            List<MemberDto> members,
            String myApplicationStatus
    ) {}

    public record WeeklyGuideRequest(
            Integer week,
            String title,
            String content
    ) {}

    public record CreateGatheringRequest(
            String type,
            String category,
            String title,
            String shortDescription,
            String description,
            List<String> tags,
            String goal,
            Integer maxMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            List<WeeklyGuideRequest> weeklyGuides
    ) {}

    public record UpdateGatheringRequest(
            String type,
            String category,
            String title,
            String shortDescription,
            String description,
            List<String> tags,
            String goal,
            Integer maxMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            List<WeeklyGuideRequest> weeklyGuides
    ) {}

    public record GatheringListResponse(
            List<GatheringSummaryDto> gatherings,
            Integer totalCount,
            Integer totalPages,
            Integer currentPage
    ) {}

    public record MainGatheringResponse(
            List<GatheringSummaryDto> popular,
            List<GatheringSummaryDto> deadline,
            List<GatheringSummaryDto> latest
    ) {}
}
