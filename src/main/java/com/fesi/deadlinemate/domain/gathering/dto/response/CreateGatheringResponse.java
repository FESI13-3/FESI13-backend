package com.fesi.deadlinemate.domain.gathering.dto.response;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
public record CreateGatheringResponse(
        Long id,
        String type,
        List<String> categories,
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
        GatheringStatus status,
        List<String> imageUrls
) {
    public static CreateGatheringResponse from(Gathering gathering, List<String> categories, List<String> tags, List<String> imageUrls) {
        return CreateGatheringResponse.builder()
                .id(gathering.getId())
                .type(gathering.getType().getDisplayName())
                .categories(categories == null ? List.of() : categories)
                .title(gathering.getTitle())
                .shortDescription(gathering.getShortDescription())
                .description(gathering.getDescription())
                .tags(tags == null ? List.of() : tags)
                .goal(gathering.getGoal())
                .maxMembers(gathering.getMaxMembers())
                .currentMembers(gathering.getCurrentMembers())
                .recruitDeadline(gathering.getRecruitDeadline())
                .startDate(gathering.getStartDate())
                .endDate(gathering.getEndDate())
                .totalWeeks(gathering.getTotalWeeks())
                .status(gathering.getStatus())
                .imageUrls(imageUrls == null ? List.of() : imageUrls)
                .build();
    }
}