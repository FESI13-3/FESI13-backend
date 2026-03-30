package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.client.dto.MyGatheringListInfo;
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
    public MyGatheringListInfo getMyGatherings(Long userId, String status, int page, int limit) {
        return MyGatheringListInfo.from(
                membershipQueryService.getMyGatherings(userId, status, page, limit)
        );
    }
}
