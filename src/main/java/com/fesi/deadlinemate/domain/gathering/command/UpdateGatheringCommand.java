package com.fesi.deadlinemate.domain.gathering.command;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record UpdateGatheringCommand(
        Long requesterId,
        GatheringType type,
        List<Long> categoryIds,
        String title,
        String shortDescription,
        String description,
        String goal,
        List<String> tags,
        int maxMembers,
        LocalDate recruitDeadline,
        LocalDate startDate,
        LocalDate endDate,
        List<UpdateWeeklyGuideCommand> weeklyGuides,
        List<String> imageUrls
) {
    public record UpdateWeeklyGuideCommand(
            int week,
            String title,
            String content
    ) {
    }
}