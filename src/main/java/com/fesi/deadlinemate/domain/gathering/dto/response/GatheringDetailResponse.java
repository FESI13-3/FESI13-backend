package com.fesi.deadlinemate.domain.gathering.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatheringDetailResponse {
    private Long id;
    private String type;
    private String category;
    private String title;
    private String shortDescription;
    private String description;
    private String goal;
    private List<String> tags;
    private Integer maxMembers;
    private Integer currentMembers;
    private LocalDate recruitDeadline;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWeeks;
    private String status;
}
