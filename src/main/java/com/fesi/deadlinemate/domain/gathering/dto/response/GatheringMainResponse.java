package com.fesi.deadlinemate.domain.gathering.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record GatheringMainResponse(
        List<GatheringListItemResponse> popular,
        List<GatheringListItemResponse> deadline,
        List<GatheringListItemResponse> latest
) {
    public static GatheringMainResponse of(
            List<GatheringListItemResponse> popular,
            List<GatheringListItemResponse> deadline,
            List<GatheringListItemResponse> latest
    ) {
        return GatheringMainResponse.builder()
                .popular(popular)
                .deadline(deadline)
                .latest(latest)
                .build();
    }
}
