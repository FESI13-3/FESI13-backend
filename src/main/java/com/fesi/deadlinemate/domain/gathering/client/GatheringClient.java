package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GatheringClient {

    Optional<GatheringInfo> findById(Long gatheringId);

    MyGatheringListResponse getMyGatherings(Long userId, String status, String sort, int page, int limit);
    Map<Long, String> findTitlesByIds(List<Long> gatheringIds);
    boolean isMember(Long gatheringId, Long userId);
}
