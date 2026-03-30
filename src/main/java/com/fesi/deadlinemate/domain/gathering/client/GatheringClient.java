package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import java.util.Optional;

public interface GatheringClient {

    Optional<GatheringInfo> findById(Long gatheringId);
}
