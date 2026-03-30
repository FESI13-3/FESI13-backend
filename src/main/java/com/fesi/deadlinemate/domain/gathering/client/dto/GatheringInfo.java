package com.fesi.deadlinemate.domain.gathering.client.dto;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatheringInfo {

    private final Long id;
    private final String title;
    private final GatheringStatus status;

    public static GatheringInfo from(Gathering gathering) {
        return GatheringInfo.builder()
                .id(gathering.getId())
                .title(gathering.getTitle())
                .status(gathering.getStatus())
                .build();
    }

    public boolean isCompleted() {
        return this.status == GatheringStatus.COMPLETED;
    }
}
