package com.fesi.deadlinemate.domain.gathering.dto.response;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record GatheringDetailResponse(
        Long id,
        String type,
        String category,
        String title,
        String shortDescription,
        String description,
        List<String> tags,
        String goal,
        int maxMembers,
        int currentMembers,
        LocalDate recruitDeadline,
        LocalDate startDate,
        LocalDate endDate,
        int totalWeeks,
        List<ImageResponse> images,
        GatheringStatus status,
        LeaderResponse leader,
        List<WeeklyPlanResponse> weeklyPlans,
        List<MemberResponse> members
) {
    @Builder
    public record ImageResponse(
            String url,
            int displayOrder
    ) {
    }

    @Builder
    public record LeaderResponse(
            Long id,
            String nickname,
            String profileImage
    ) {
    }

    @Builder
    public record WeeklyPlanResponse(
            int week,
            String title,
            LocalDate startDate,
            LocalDate endDate
    ) {
    }

    @Builder
    public record MemberResponse(
            Long userId,
            String nickname,
            String profileImage,
            GatheringRole role
    ) {
    }
}
