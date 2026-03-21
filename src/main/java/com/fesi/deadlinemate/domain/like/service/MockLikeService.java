package com.fesi.deadlinemate.domain.like.service;

import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAuthContext;
import com.fesi.deadlinemate.global.mock.support.MockFinder;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MockLikeService {
    private final MockStore store;
    private final MockFinder finder;
    private final MockAuthContext authContext;

    public MockLikeService(
            MockStore store,
            MockFinder finder,
            MockAuthContext authContext
    ) {
        this.store = store;
        this.finder = finder;
        this.authContext = authContext;
    }

    public void like(Long gatheringId) {
        finder.getGathering(gatheringId);

        Set<Long> likes = store.gatheringLikes.computeIfAbsent(gatheringId, k -> new LinkedHashSet<>());
        if (!likes.add(authContext.currentUserId())) {
            throw new IllegalStateException("이미 찜한 모임입니다.");
        }
    }

    public void unlike(Long gatheringId) {
        finder.getGathering(gatheringId);

        Set<Long> likes = store.gatheringLikes.computeIfAbsent(gatheringId, k -> new LinkedHashSet<>());
        if (!likes.remove(authContext.currentUserId())) {
            throw new NoSuchElementException("찜한 이력이 없습니다.");
        }
    }
}
