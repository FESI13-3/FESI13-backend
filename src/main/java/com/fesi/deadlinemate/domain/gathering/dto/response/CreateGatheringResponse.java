package com.fesi.deadlinemate.domain.gathering.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateGatheringResponse {
    private GatheringDetailResponse gathering;
}
