package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GatheringClient {

    Optional<GatheringInfo> findById(Long gatheringId);

    Map<Long, String> findTitlesByIds(List<Long> gatheringIds);

    boolean isMember(Long gatheringId, Long userId);
}
