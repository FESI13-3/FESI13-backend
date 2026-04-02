package com.fesi.deadlinemate.domain.gathering.projection;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record GatheringListRow(
        Long id,
        Long leaderId,
        GatheringType type,
        String title,
        String shortDescription,
        int maxMembers,
        int currentMembers,
        LocalDate recruitDeadline,
        LocalDate startDate,
        LocalDate endDate,
        GatheringStatus status
) {
}
