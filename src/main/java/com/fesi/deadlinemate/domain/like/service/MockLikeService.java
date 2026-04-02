package com.fesi.deadlinemate.domain.like.service;

import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.GatheringSummaryDto;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.LeaderDto;
import com.fesi.deadlinemate.domain.gathering.entity.MockGatheringEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAuthContext;
import com.fesi.deadlinemate.global.mock.support.MockFinder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    public List<Long> getLikedGatheringIds() {
        Long userId = authContext.currentUserId();
        return store.gatheringLikes.entrySet().stream()
                .filter(entry -> entry.getValue().contains(userId))
                .map(Map.Entry::getKey)
                .toList();
    }

    public Map<String, Object> getMyLikedGatherings(int page, int limit) {
        Long userId = authContext.currentUserId();

        List<Long> likedGatheringIds = store.gatheringLikes.entrySet().stream()
                .filter(entry -> entry.getValue().contains(userId))
                .map(Map.Entry::getKey)
                .toList();

        List<GatheringSummaryDto> allGatherings = likedGatheringIds.stream()
                .map(id -> store.gatherings.get(id))
                .filter(g -> g != null)
                .map(this::toSummary)
                .toList();

        int total = allGatherings.size();
        int totalPages = (int) Math.ceil((double) total / limit);
        int fromIndex = Math.min((page - 1) * limit, total);
        int toIndex = Math.min(fromIndex + limit, total);

        return Map.of(
                "gatherings", allGatherings.subList(fromIndex, toIndex),
                "totalCount", total,
                "totalPages", totalPages,
                "currentPage", page
        );
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

    private GatheringSummaryDto toSummary(MockGatheringEntity g) {
        return new GatheringSummaryDto(
                g.id,
                g.type,
                g.category,
                g.title,
                g.shortDescription,
                g.tags,
                g.maxMembers,
                store.gatheringMembers.getOrDefault(g.id, Set.of()).size(),
                g.recruitDeadline,
                g.startDate,
                g.endDate,
                g.status,
                new LeaderDto(g.leaderId, "마감왕", "https://example.com/profile.png")
        );
    }
}
