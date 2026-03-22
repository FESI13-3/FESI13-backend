package com.fesi.deadlinemate.domain.gathering.dto.response;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder
public record UpdateGatheringResponse(
        Long id,
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
        GatheringStatus status,
        List<String> tags
) {
    public static UpdateGatheringResponse from(Gathering gathering, List<String> tags) {
        return UpdateGatheringResponse.builder()
                .id(gathering.getId())
                .type(gathering.getType())
                .category(gathering.getCategory())
                .title(gathering.getTitle())
                .shortDescription(gathering.getShortDescription())
                .description(gathering.getDescription())
                .goal(gathering.getGoal())
                .maxMembers(gathering.getMaxMembers())
                .currentMembers(gathering.getCurrentMembers())
                .recruitDeadline(gathering.getRecruitDeadline())
                .startDate(gathering.getStartDate())
                .endDate(gathering.getEndDate())
                .totalWeeks(gathering.getTotalWeeks())
                .status(gathering.getStatus())
                .tags(tags)
                .build();
    }
}