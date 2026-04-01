package com.fesi.deadlinemate.domain.gathering.dto.response;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record GatheringListItemResponse(
        Long id,
        String type,
        String category,
        String title,
        String shortDescription,
        List<String> tags,
        int maxMembers,
        int currentMembers,
        LocalDate recruitDeadline,
        LocalDate startDate,
        LocalDate endDate,
        GatheringStatus status,
        LeaderSummary leader
) {
    @Builder
    public record LeaderSummary(
            Long id,
            String nickname,
            String profileImage
    ) {
    }
}
