package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import java.util.Optional;

public interface GatheringClient {

    Optional<GatheringInfo> findById(Long gatheringId);

    MyGatheringListResponse getMyGatherings(Long userId, String status, int page, int limit);
}
