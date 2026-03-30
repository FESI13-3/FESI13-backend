package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringInternalClient implements GatheringClient {

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository gatheringMemberRepository;

    @Override
    public Optional<GatheringInfo> findById(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .map(GatheringInfo::from);
    }

    @Override
    public boolean isMember(Long gatheringId, Long userId) {
        return gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(gatheringId, userId);
    }
}
