package com.fesi.deadlinemate.domain.gathering.dto.request;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class CreateGatheringRequest {
    @NotNull(message = "모임 유형은 필수입니다.")
    private GatheringType type;

    @NotBlank(message = "카테고리는 필수입니다.")
    @Size(max = 50, message = "카테고리는 50자 이하여야 합니다.")
    private String category;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 2, max = 60, message = "제목은 2자 이상 60자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "한 줄 소개는 필수입니다.")
    @Size(min = 2, max = 100, message = "한 줄 소개는 2자 이상 100자 이하여야 합니다.")
    private String shortDescription;

    @NotBlank(message = "상세 설명은 필수입니다.")
    @Size(min = 10, max = 1000, message = "상세 설명은 10자 이상 1000자 이하여야 합니다.")
    private String description;

    @NotBlank(message = "최종 목표는 필수입니다.")
    private String goal;

    @Size(max = 10, message = "태그는 최대 10개까지 가능합니다.")
    private List<String> tags;

    @NotNull(message = "최대 인원은 필수입니다.")
    @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
    @Max(value = 10, message = "최대 인원은 10명 이하여야 합니다.")
    private Integer maxMembers;

    @NotNull(message = "모집 마감일은 필수입니다.")
    private LocalDate recruitDeadline;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

   /* @Valid
    @NotEmpty(message = "주차별 계획은 최소 1개 이상 필요합니다.")
    private List<WeeklyGuideRequest> weeklyGuides;*/
}
