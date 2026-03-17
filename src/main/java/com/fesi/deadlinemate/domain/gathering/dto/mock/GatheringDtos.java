package com.fesi.deadlinemate.domain.gathering.dto.mock;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GatheringDtos {
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

    public record WeeklyGuideRequest(
            Integer week,
            String title,
            String content
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

    public static class GatheringEntity {
        public Long id;
        public String type;
        public String category;
        public String title;
        public String shortDescription;
        public String description;
        public List<String> tags = new ArrayList<>();
        public String goal;
        public Integer maxMembers;
        public Integer currentMembers;
        public LocalDate recruitDeadline;
        public LocalDate startDate;
        public LocalDate endDate;
        public String status;
        public Long leaderId;
        public List<WeeklyGuideRequest> weeklyGuides = new ArrayList<>();
        public List<Map<String, Object>> images = new ArrayList<>();
        public OffsetDateTime createdAt;
    }
}
