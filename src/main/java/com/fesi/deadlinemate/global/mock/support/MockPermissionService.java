package com.fesi.deadlinemate.global.mock.support;

import com.fesi.deadlinemate.domain.gathering.entity.MockGatheringEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MockPermissionService {
    private final MockStore store;
    private final MockAuthContext authContext;

    public MockPermissionService(MockStore store, MockAuthContext authContext) {
        this.store = store;
        this.authContext = authContext;
    }

    public void validateLeader(MockGatheringEntity gathering) {
        if (!Objects.equals(gathering.leaderId, authContext.currentUserId())) {
            throw new SecurityException("모임장만 접근할 수 있습니다.");
        }
    }

    public void validateMember(Long gatheringId) {
        Set<Long> members = store.gatheringMembers.getOrDefault(gatheringId, Set.of());
        if (!members.contains(authContext.currentUserId())) {
            throw new SecurityException("참여 멤버만 접근할 수 있습니다.");
        }
    }
}
