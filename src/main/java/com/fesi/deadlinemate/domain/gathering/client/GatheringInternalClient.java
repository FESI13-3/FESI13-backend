package com.fesi.deadlinemate.domain.gathering.client;

import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatheringInternalClient implements GatheringClient {

    private final GatheringRepository gatheringRepository;

    @Override
    public Optional<GatheringInfo> findById(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .map(GatheringInfo::from);
    }
}
