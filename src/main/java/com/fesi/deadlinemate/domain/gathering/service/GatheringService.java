package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;

public interface GatheringService {
    CreateGatheringResponse createGathering(Long userId, CreateGatheringRequest request);
}
