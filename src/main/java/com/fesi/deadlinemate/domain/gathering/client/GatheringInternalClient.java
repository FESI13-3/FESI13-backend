package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.service.MembershipQueryService;
import java.util.Optional;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringInternalClient implements GatheringClient {

    private final GatheringRepository gatheringRepository;
    private final MembershipQueryService membershipQueryService;
    private final GatheringMemberRepository gatheringMemberRepository;

    @Override
    public Optional<GatheringInfo> findById(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .map(GatheringInfo::from);
    }

    @Override
    public MyGatheringListResponse getMyGatherings(Long userId, String status, String sort, int page, int limit) {
        return membershipQueryService.getMyGatherings(userId, status, sort, page, limit);
    }

    @Override
    public Map<Long, String> findTitlesByIds(List<Long> gatheringIds) {
        return gatheringRepository.findByIdIn(gatheringIds).stream()
                .collect(Collectors.toMap(Gathering::getId, Gathering::getTitle));
    }

    @Override
    public boolean isMember(Long gatheringId, Long userId) {
        return gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(gatheringId, userId);
    }
}
