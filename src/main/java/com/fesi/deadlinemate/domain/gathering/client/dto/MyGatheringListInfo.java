package com.fesi.deadlinemate.domain.gathering.client.dto;

import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;

public record MyGatheringListInfo(
        MyGatheringListResponse response
) {
    public static MyGatheringListInfo from(MyGatheringListResponse response) {
        return new MyGatheringListInfo(response);
    }
}
