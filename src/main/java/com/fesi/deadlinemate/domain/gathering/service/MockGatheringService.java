package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.GatheringDetailDto;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.GatheringSummaryDto;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.LeaderDto;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.MainGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.MemberDto;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.WeeklyPlanDto;
import com.fesi.deadlinemate.domain.gathering.entity.MockGatheringEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAuthContext;
import com.fesi.deadlinemate.global.mock.support.MockFinder;
import com.fesi.deadlinemate.global.mock.support.MockPermissionService;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MockGatheringService {
    private final MockStore store;
    private final MockFinder finder;
    private final MockAuthContext authContext;
    private final MockPermissionService permissionService;

    public MockGatheringService(
            MockStore store,
            MockFinder finder,
            MockAuthContext authContext,
            MockPermissionService permissionService
    ) {
        this.store = store;
        this.finder = finder;
        this.authContext = authContext;
        this.permissionService = permissionService;
    }

    public GatheringListResponse getGatherings(String type, String category, String sort, String status,
                                               String query, int page, int limit) {

        List<GatheringSummaryDto> filtered = store.gatherings.values().stream()
                .filter(g -> type == null || g.type.equals(type))
                .filter(g -> category == null || g.category.equals(category))
                .filter(g -> status == null || "all".equalsIgnoreCase(status) || g.status.equalsIgnoreCase("RECRUITING"))
                .filter(g -> query == null || g.title.contains(query)
                        || g.shortDescription.contains(query)
                        || g.tags.stream().anyMatch(tag -> tag.contains(query)))
                .sorted(Comparator.comparing((MockGatheringEntity g) -> g.createdAt).reversed())
                .map(this::toSummary)
                .toList();

        int totalCount = filtered.size();
        int totalPages = (int) Math.ceil((double) totalCount / limit);
        int from = Math.max((page - 1) * limit, 0);
        int to = Math.min(from + limit, totalCount);

        List<GatheringSummaryDto> pageItems = from >= totalCount ? List.of() : filtered.subList(from, to);

        return new GatheringListResponse(
                pageItems,
                totalCount,
                totalPages == 0 ? 1 : totalPages,
                page
        );
    }

    public MainGatheringResponse getMain(int limit) {
        List<GatheringSummaryDto> list = store.gatherings.values().stream()
                .map(this::toSummary)
                .limit(limit)
                .toList();

        return new MainGatheringResponse(list, list, list);
    }

    public GatheringDetailDto getGatheringDetail(Long gatheringId) {
        MockGatheringEntity gathering = finder.getGathering(gatheringId);
        return toDetail(gathering, authContext.currentUserId());
    }

    public GatheringDetailDto createGathering(CreateGatheringRequest req) {
        MockGatheringEntity gathering = new MockGatheringEntity();
        gathering.id = store.gatheringSeq.getAndIncrement();
        gathering.type = req.type();
        gathering.category = req.category();
        gathering.title = req.title();
        gathering.shortDescription = req.shortDescription();
        gathering.description = req.description();
        gathering.tags = req.tags() == null ? new ArrayList<>() : req.tags();
        gathering.goal = req.goal();
        gathering.maxMembers = req.maxMembers();
        gathering.currentMembers = 1;
        gathering.recruitDeadline = req.recruitDeadline();
        gathering.startDate = req.startDate();
        gathering.endDate = req.endDate();
        gathering.status = "RECRUITING";
        gathering.leaderId = authContext.currentUserId();
        gathering.weeklyGuides = req.weeklyGuides() == null ? new ArrayList<>() : req.weeklyGuides();
        gathering.images = List.of(Map.of("url", "https://example.com/default.jpg", "displayOrder", 0));
        gathering.createdAt = OffsetDateTime.now();

        store.gatherings.put(gathering.id, gathering);
        store.gatheringMembers.put(gathering.id, new LinkedHashSet<>(List.of(authContext.currentUserId())));
        store.gatheringLikes.put(gathering.id, new LinkedHashSet<>());

        return toDetail(gathering, authContext.currentUserId());
    }

    public GatheringDetailDto updateGathering(Long gatheringId, UpdateGatheringRequest req) {
        MockGatheringEntity gathering = finder.getGathering(gatheringId);
        permissionService.validateLeader(gathering);

        if (req.type() != null) gathering.type = req.type();
        if (req.category() != null) gathering.category = req.category();
        if (req.title() != null) gathering.title = req.title();
        if (req.shortDescription() != null) gathering.shortDescription = req.shortDescription();
        if (req.description() != null) gathering.description = req.description();
        if (req.tags() != null) gathering.tags = req.tags();
        if (req.goal() != null) gathering.goal = req.goal();
        if (req.maxMembers() != null) gathering.maxMembers = req.maxMembers();
        if (req.recruitDeadline() != null) gathering.recruitDeadline = req.recruitDeadline();
        if (req.startDate() != null) gathering.startDate = req.startDate();
        if (req.endDate() != null) gathering.endDate = req.endDate();
        if (req.weeklyGuides() != null) gathering.weeklyGuides = req.weeklyGuides();

        return toDetail(gathering, authContext.currentUserId());
    }

    public void deleteGathering(Long gatheringId) {
        MockGatheringEntity gathering = finder.getGathering(gatheringId);
        permissionService.validateLeader(gathering);

        if ("IN_PROGRESS".equals(gathering.status)) {
            throw new IllegalArgumentException("진행 중인 모임은 삭제할 수 없습니다.");
        }

        store.gatherings.remove(gatheringId);
        store.gatheringMembers.remove(gatheringId);
        store.gatheringLikes.remove(gatheringId);
        store.applications.values().removeIf(a -> a.gatheringId.equals(gatheringId));
        store.todos.values().removeIf(t -> t.gatheringId.equals(gatheringId));
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

    private GatheringDetailDto toDetail(MockGatheringEntity g, Long userId) {
        List<MemberDto> members = store.gatheringMembers.getOrDefault(g.id, Set.of()).stream()
                .map(id -> new MemberDto(
                        id,
                        "유저" + id,
                        "https://example.com/profile.png",
                        Objects.equals(id, g.leaderId) ? "LEADER" : "MEMBER",
                        85.0,
                        true
                ))
                .toList();

        List<WeeklyPlanDto> weeklyPlans = g.weeklyGuides.stream()
                .map(w -> new WeeklyPlanDto(
                        w.week(),
                        w.title(),
                        g.startDate.plusWeeks(w.week() - 1L),
                        g.startDate.plusWeeks(w.week() - 1L).plusDays(6)
                ))
                .toList();

        String myApplicationStatus = store.applications.values().stream()
                .filter(a -> a.gatheringId.equals(g.id) && a.applicantId.equals(userId))
                .map(a -> a.status)
                .findFirst()
                .orElse(null);

        return new GatheringDetailDto(
                g.id,
                g.type,
                g.category,
                g.title,
                g.shortDescription,
                g.description,
                g.tags,
                g.goal,
                g.maxMembers,
                store.gatheringMembers.getOrDefault(g.id, Set.of()).size(),
                g.recruitDeadline,
                g.startDate,
                g.endDate,
                (int) ChronoUnit.WEEKS.between(g.startDate, g.endDate) + 1,
                g.images,
                g.status,
                new LeaderDto(g.leaderId, "마감왕", "https://example.com/profile.png"),
                weeklyPlans,
                members,
                myApplicationStatus
        );
    }
}
