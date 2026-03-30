package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.service.MembershipQueryService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringInternalClient implements GatheringClient {

    private final GatheringRepository gatheringRepository;
    private final MembershipQueryService membershipQueryService;

    @Override
    public Optional<GatheringInfo> findById(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .map(GatheringInfo::from);
    }

    @Override
    public MyGatheringListResponse getMyGatherings(Long userId, String status, int page, int limit) {
        return membershipQueryService.getMyGatherings(userId, status, page, limit);
    }
}
