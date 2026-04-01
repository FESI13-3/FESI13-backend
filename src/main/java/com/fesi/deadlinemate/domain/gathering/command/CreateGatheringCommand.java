package com.fesi.deadlinemate.domain.gathering.command;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record CreateGatheringCommand(
        Long leaderId,
        GatheringType type,
        String category,
        String title,
        String shortDescription,
        String description,
        List<String> tags,
        String goal,
        int maxMembers,
        LocalDate recruitDeadline,
        LocalDate startDate,
        LocalDate endDate,
        List<CreateWeeklyGuideCommand> weeklyGuides,
        List<String> imageUrls
) {
    public record CreateWeeklyGuideCommand(
            int week,
            String title,
            String content
    ) {
    }
}