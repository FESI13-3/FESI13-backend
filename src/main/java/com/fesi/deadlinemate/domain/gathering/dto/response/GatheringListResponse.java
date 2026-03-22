package com.fesi.deadlinemate.domain.gathering.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record GatheringListResponse(
        List<GatheringListItemResponse> gatherings,
        long totalCount,
        int totalPages,
        int currentPage
) {
    public static GatheringListResponse of(List<GatheringListItemResponse> gatherings, long totalCount, int totalPages, int currentPage) {
        return GatheringListResponse.builder()
                .gatherings(gatherings)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }
}
