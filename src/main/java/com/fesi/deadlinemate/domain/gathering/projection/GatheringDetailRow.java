package com.fesi.deadlinemate.domain.gathering.projection;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record GatheringDetailRow(
        Long id,
        Long leaderId,
        GatheringType type,
        String category,
        String title,
        String shortDescription,
        String description,
        String goal,
        int maxMembers,
        int currentMembers,
        LocalDate recruitDeadline,
        LocalDate startDate,
        LocalDate endDate,
        int totalWeeks,
        GatheringStatus status
) {
}
