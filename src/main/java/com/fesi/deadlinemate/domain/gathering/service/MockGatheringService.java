package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.dto.mock.AchievementDtos.AchievementResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.AchievementDtos.MemberAchievementDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.AchievementDtos.RankingItemDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.AchievementDtos.RankingResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.AchievementDtos.WeeklyRateDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.ApplicantDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.ApplicationEntity;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.ApplicationItemDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.ApplicationListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.CreateApplicationRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.MyApplicationItemDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.MyApplicationListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.UpdateApplicationStatusRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.GatheringDetailDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.GatheringEntity;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.GatheringSummaryDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.LeaderDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.MainGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.MemberDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.WeeklyPlanDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ReportDtos.MemberResultDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ReportDtos.ReportGatheringDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ReportDtos.ReportResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.CreateTodoRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.MyTodoResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.TodoEntity;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.TodoItemDto;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.TodoListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.UpdateTodoRequest;
import com.fesi.deadlinemate.domain.gathering.repository.mock.MockStore;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MockGatheringService {
    private final MockStore store;

    public MockGatheringService(MockStore store) {
        this.store = store;
    }

    public Long currentUserId() {
        return 1L;
    }

    public GatheringListResponse getGatherings(String type, String category, String sort, String status,
                                               String query, int page, int limit) {
        List<GatheringSummaryDto> filtered = store.gatherings.values().stream()
                .filter(g -> type == null || g.type.equals(type))
                .filter(g -> category == null || g.category.equals(category))
                .filter(g -> status == null || "all".equalsIgnoreCase(status) || g.status.equalsIgnoreCase("RECRUITING"))
                .filter(g -> query == null || g.title.contains(query) || g.shortDescription.contains(query)
                        || g.tags.stream().anyMatch(tag -> tag.contains(query)))
                .sorted(Comparator.comparing((GatheringEntity g) -> g.createdAt).reversed())
                .map(this::toSummary)
                .toList();

        int totalCount = filtered.size();
        int totalPages = (int) Math.ceil((double) totalCount / limit);
        int from = Math.max((page - 1) * limit, 0);
        int to = Math.min(from + limit, totalCount);

        List<GatheringSummaryDto> pageItems = from >= totalCount ? List.of() : filtered.subList(from, to);

        return new GatheringListResponse(pageItems, totalCount, totalPages == 0 ? 1 : totalPages, page);
    }

    public MainGatheringResponse getMain(int limit) {
        List<GatheringSummaryDto> list = store.gatherings.values().stream()
                .map(this::toSummary)
                .limit(limit)
                .toList();

        return new MainGatheringResponse(list, list, list);
    }

    public GatheringDetailDto getGatheringDetail(Long gatheringId) {
        GatheringEntity g = getGathering(gatheringId);
        return toDetail(g, currentUserId());
    }

    public GatheringDetailDto createGathering(CreateGatheringRequest req) {
        GatheringEntity g = new GatheringEntity();
        g.id = store.gatheringSeq.getAndIncrement();
        g.type = req.type();
        g.category = req.category();
        g.title = req.title();
        g.shortDescription = req.shortDescription();
        g.description = req.description();
        g.tags = req.tags() == null ? new ArrayList<>() : req.tags();
        g.goal = req.goal();
        g.maxMembers = req.maxMembers();
        g.currentMembers = 1;
        g.recruitDeadline = req.recruitDeadline();
        g.startDate = req.startDate();
        g.endDate = req.endDate();
        g.status = "RECRUITING";
        g.leaderId = currentUserId();
        g.weeklyGuides = req.weeklyGuides() == null ? new ArrayList<>() : req.weeklyGuides();
        g.images = List.of(Map.of("url", "https://example.com/default.jpg", "displayOrder", 0));
        g.createdAt = OffsetDateTime.now();

        store.gatherings.put(g.id, g);
        store.gatheringMembers.put(g.id, new LinkedHashSet<>(List.of(currentUserId())));
        store.gatheringLikes.put(g.id, new LinkedHashSet<>());

        return toDetail(g, currentUserId());
    }

    public GatheringDetailDto updateGathering(Long gatheringId, UpdateGatheringRequest req) {
        GatheringEntity g = getGathering(gatheringId);
        validateLeader(g);

        if (req.type() != null) g.type = req.type();
        if (req.category() != null) g.category = req.category();
        if (req.title() != null) g.title = req.title();
        if (req.shortDescription() != null) g.shortDescription = req.shortDescription();
        if (req.description() != null) g.description = req.description();
        if (req.tags() != null) g.tags = req.tags();
        if (req.goal() != null) g.goal = req.goal();
        if (req.maxMembers() != null) g.maxMembers = req.maxMembers();
        if (req.recruitDeadline() != null) g.recruitDeadline = req.recruitDeadline();
        if (req.startDate() != null) g.startDate = req.startDate();
        if (req.endDate() != null) g.endDate = req.endDate();
        if (req.weeklyGuides() != null) g.weeklyGuides = req.weeklyGuides();

        return toDetail(g, currentUserId());
    }

    public void deleteGathering(Long gatheringId) {
        GatheringEntity g = getGathering(gatheringId);
        validateLeader(g);
        if ("IN_PROGRESS".equals(g.status)) {
            throw new IllegalArgumentException("진행 중인 모임은 삭제할 수 없습니다.");
        }
        store.gatherings.remove(gatheringId);
        store.gatheringMembers.remove(gatheringId);
        store.gatheringLikes.remove(gatheringId);
        store.applications.values().removeIf(a -> a.gatheringId.equals(gatheringId));
        store.todos.values().removeIf(t -> t.gatheringId.equals(gatheringId));
    }

    public Map<String, Object> createApplication(Long gatheringId, CreateApplicationRequest req) {
        GatheringEntity g = getGathering(gatheringId);

        boolean exists = store.applications.values().stream()
                .anyMatch(a -> a.gatheringId.equals(gatheringId)
                        && a.applicantId.equals(currentUserId())
                        && "PENDING".equals(a.status));
        if (exists) {
            throw new IllegalStateException("이미 신청 중입니다.");
        }

        ApplicationEntity a = new ApplicationEntity();
        a.id = store.applicationSeq.getAndIncrement();
        a.gatheringId = gatheringId;
        a.applicantId = currentUserId();
        a.personalGoal = req.personalGoal();
        a.selfIntroduction = req.selfIntroduction();
        a.status = "PENDING";
        a.createdAt = OffsetDateTime.now();

        store.applications.put(a.id, a);

        return Map.of(
                "application", Map.of(
                        "id", a.id,
                        "status", a.status,
                        "createdAt", a.createdAt
                )
        );
    }

    public ApplicationListResponse getApplications(Long gatheringId) {
        GatheringEntity g = getGathering(gatheringId);
        validateLeader(g);

        List<ApplicationItemDto> items = store.applications.values().stream()
                .filter(a -> a.gatheringId.equals(gatheringId))
                .sorted(Comparator.comparing((ApplicationEntity a) -> a.createdAt).reversed())
                .map(a -> new ApplicationItemDto(
                        a.id,
                        new ApplicantDto(a.applicantId, "유저" + a.applicantId, "https://example.com/profile.png", 36.5),
                        a.personalGoal,
                        a.selfIntroduction,
                        a.status,
                        a.createdAt
                ))
                .toList();

        return new ApplicationListResponse(items);
    }

    public Map<String, Object> updateApplicationStatus(Long gatheringId, Long applicationId, UpdateApplicationStatusRequest req) {
        GatheringEntity g = getGathering(gatheringId);
        validateLeader(g);

        ApplicationEntity a = getApplication(applicationId);
        a.status = req.status();

        if ("ACCEPTED".equals(req.status())) {
            store.gatheringMembers.computeIfAbsent(gatheringId, k -> new LinkedHashSet<>()).add(a.applicantId);
            g.currentMembers = store.gatheringMembers.get(gatheringId).size();
        }

        return Map.of("application", Map.of("id", a.id, "status", a.status));
    }

    public void cancelApplication(Long gatheringId, Long applicationId) {
        ApplicationEntity a = getApplication(applicationId);
        if (!a.gatheringId.equals(gatheringId) || !a.applicantId.equals(currentUserId())) {
            throw new IllegalArgumentException("본인의 신청만 취소할 수 있습니다.");
        }
        if (!"PENDING".equals(a.status)) {
            throw new IllegalArgumentException("PENDING 상태가 아닌 경우 취소할 수 없습니다.");
        }
        store.applications.remove(applicationId);
    }

    public MyApplicationListResponse getMyApplications() {
        List<MyApplicationItemDto> items = store.applications.values().stream()
                .filter(a -> a.applicantId.equals(currentUserId()))
                .sorted(Comparator.comparing((ApplicationEntity a) -> a.createdAt).reversed())
                .map(a -> {
                    GatheringEntity g = getGathering(a.gatheringId);
                    return new MyApplicationItemDto(
                            a.id,
                            Map.of("id", g.id, "title", g.title, "type", g.type, "status", g.status),
                            a.personalGoal,
                            a.status,
                            a.createdAt
                    );
                })
                .toList();

        return new MyApplicationListResponse(items);
    }

    public TodoListResponse getTodos(Long gatheringId, Integer week) {
        validateMember(gatheringId);

        List<TodoItemDto> items = store.todos.values().stream()
                .filter(t -> t.gatheringId.equals(gatheringId))
                .filter(t -> week == null || t.week.equals(week))
                .sorted(Comparator.comparing((TodoEntity t) -> t.createdAt))
                .map(this::toTodoItem)
                .toList();

        return new TodoListResponse(items);
    }

    public MyTodoResponse getMyTodos(Long gatheringId, Integer week) {
        validateMember(gatheringId);

        List<TodoEntity> myTodos = store.todos.values().stream()
                .filter(t -> t.gatheringId.equals(gatheringId))
                .filter(t -> t.userId.equals(currentUserId()))
                .filter(t -> week == null || t.week.equals(week))
                .toList();

        List<TodoItemDto> items = myTodos.stream().map(this::toTodoItem).toList();

        double weeklyRate = calcRate(myTodos.stream()
                .filter(t -> week == null || t.week.equals(week))
                .toList());

        double overallRate = calcRate(store.todos.values().stream()
                .filter(t -> t.gatheringId.equals(gatheringId))
                .filter(t -> t.userId.equals(currentUserId()))
                .toList());

        return new MyTodoResponse(items, weeklyRate, overallRate);
    }

    public Map<String, Object> createTodo(Long gatheringId, CreateTodoRequest req) {
        validateMember(gatheringId);

        TodoEntity t = new TodoEntity();
        t.id = store.todoSeq.getAndIncrement();
        t.gatheringId = gatheringId;
        t.userId = currentUserId();
        t.week = req.week();
        t.content = req.content();
        t.isCompleted = false;
        t.createdAt = OffsetDateTime.now();

        store.todos.put(t.id, t);

        return Map.of("todo", toTodoItem(t));
    }

    public Map<String, Object> updateTodo(Long gatheringId, Long todoId, UpdateTodoRequest req) {
        TodoEntity t = getTodo(todoId);
        if (!t.gatheringId.equals(gatheringId) || !t.userId.equals(currentUserId())) {
            throw new IllegalArgumentException("본인 Todo만 수정할 수 있습니다.");
        }

        if (req.content() != null) t.content = req.content();
        if (req.isCompleted() != null) t.isCompleted = req.isCompleted();

        return Map.of("todo", Map.of(
                "id", t.id,
                "content", t.content,
                "isCompleted", t.isCompleted
        ));
    }

    public void deleteTodo(Long gatheringId, Long todoId) {
        TodoEntity t = getTodo(todoId);
        if (!t.gatheringId.equals(gatheringId) || !t.userId.equals(currentUserId())) {
            throw new IllegalArgumentException("본인 Todo만 삭제할 수 있습니다.");
        }
        store.todos.remove(todoId);
    }

    public AchievementResponse getAchievements(Long gatheringId) {
        validateMember(gatheringId);

        Set<Long> members = store.gatheringMembers.getOrDefault(gatheringId, Set.of());

        List<MemberAchievementDto> memberAchievements = members.stream()
                .map(userId -> {
                    List<TodoEntity> userTodos = store.todos.values().stream()
                            .filter(t -> t.gatheringId.equals(gatheringId))
                            .filter(t -> t.userId.equals(userId))
                            .toList();

                    Map<Integer, List<TodoEntity>> byWeek = userTodos.stream()
                            .collect(Collectors.groupingBy(t -> t.week));

                    List<WeeklyRateDto> weeklyRates = byWeek.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(e -> new WeeklyRateDto(e.getKey(), calcRate(e.getValue())))
                            .toList();

                    return new MemberAchievementDto(
                            userId,
                            "유저" + userId,
                            weeklyRates,
                            calcRate(userTodos)
                    );
                })
                .toList();

        List<WeeklyRateDto> teamWeeklyRates = List.of(
                new WeeklyRateDto(1, 90.0),
                new WeeklyRateDto(2, 75.0),
                new WeeklyRateDto(3, 66.7)
        );

        double teamOverall = memberAchievements.stream()
                .mapToDouble(MemberAchievementDto::overallRate)
                .average()
                .orElse(0.0);

        return new AchievementResponse(memberAchievements, teamWeeklyRates, round(teamOverall));
    }

    public RankingResponse getAchievementRanking(Long gatheringId) {
        validateMember(gatheringId);

        List<RankingItemDto> ranking = store.gatheringMembers.getOrDefault(gatheringId, Set.of()).stream()
                .map(userId -> {
                    List<TodoEntity> userTodos = store.todos.values().stream()
                            .filter(t -> t.gatheringId.equals(gatheringId))
                            .filter(t -> t.userId.equals(userId))
                            .toList();
                    return new RankingItemDto(
                            0,
                            userId,
                            "유저" + userId,
                            "https://example.com/profile.png",
                            calcRate(userTodos)
                    );
                })
                .sorted(Comparator.comparing(RankingItemDto::overallRate).reversed())
                .collect(Collectors.toList());

        List<RankingItemDto> ranked = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            RankingItemDto item = ranking.get(i);
            ranked.add(new RankingItemDto(
                    i + 1,
                    item.userId(),
                    item.nickname(),
                    item.profileImage(),
                    item.overallRate()
            ));
        }

        return new RankingResponse(ranked);
    }

    public ReportResponse getReport(Long gatheringId) {
        validateMember(gatheringId);

        GatheringEntity g = getGathering(gatheringId);

        List<MemberResultDto> memberResults = store.gatheringMembers.getOrDefault(gatheringId, Set.of()).stream()
                .map(userId -> {
                    List<TodoEntity> userTodos = store.todos.values().stream()
                            .filter(t -> t.gatheringId.equals(gatheringId))
                            .filter(t -> t.userId.equals(userId))
                            .toList();

                    int completed = (int) userTodos.stream().filter(t -> Boolean.TRUE.equals(t.isCompleted)).count();
                    int total = userTodos.size();

                    return new MemberResultDto(
                            userId,
                            "유저" + userId,
                            calcRate(userTodos),
                            completed,
                            completed,
                            total,
                            List.of(100.0, 80.0, 70.0, 90.0)
                    );
                })
                .toList();

        return new ReportResponse(
                new ReportGatheringDto(g.title, g.startDate, g.endDate),
                memberResults.stream().mapToDouble(MemberResultDto::overallRate).average().orElse(0.0),
                List.of(
                        Map.of("week", 1, "rate", 90.0),
                        Map.of("week", 2, "rate", 80.0),
                        Map.of("week", 3, "rate", 75.0),
                        Map.of("week", 4, "rate", 68.5)
                ),
                memberResults,
                Map.of(
                        "mvp", Map.of("userId", 1, "nickname", "마감왕"),
                        "longestStreak", Map.of("userId", 1, "nickname", "마감왕", "streak", 4),
                        "mostImproved", Map.of("userId", 3, "nickname", "성장맨"),
                        "attendance", Map.of("userId", 2, "nickname", "개근왕")
                )
        );
    }

    public void like(Long gatheringId) {
        getGathering(gatheringId);
        Set<Long> likes = store.gatheringLikes.computeIfAbsent(gatheringId, k -> new LinkedHashSet<>());
        if (!likes.add(currentUserId())) {
            throw new IllegalStateException("이미 찜한 모임입니다.");
        }
    }

    public void unlike(Long gatheringId) {
        getGathering(gatheringId);
        Set<Long> likes = store.gatheringLikes.computeIfAbsent(gatheringId, k -> new LinkedHashSet<>());
        if (!likes.remove(currentUserId())) {
            throw new NoSuchElementException("찜한 이력이 없습니다.");
        }
    }

    private GatheringSummaryDto toSummary(GatheringEntity g) {
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
                store.gatheringLikes.getOrDefault(g.id, Set.of()).contains(currentUserId()),
                new LeaderDto(g.leaderId, "마감왕", "https://example.com/profile.png")
        );
    }

    private GatheringDetailDto toDetail(GatheringEntity g, Long userId) {
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
                store.gatheringLikes.getOrDefault(g.id, Set.of()).contains(userId),
                new LeaderDto(g.leaderId, "마감왕", "https://example.com/profile.png"),
                weeklyPlans,
                members,
                myApplicationStatus
        );
    }

    private TodoItemDto toTodoItem(TodoEntity t) {
        return new TodoItemDto(
                t.id,
                t.userId,
                "유저" + t.userId,
                t.week,
                t.content,
                t.isCompleted,
                t.createdAt
        );
    }

    private double calcRate(List<TodoEntity> todos) {
        if (todos == null || todos.isEmpty()) return 0.0;
        long completed = todos.stream().filter(t -> Boolean.TRUE.equals(t.isCompleted)).count();
        return round((completed * 100.0) / todos.size());
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private GatheringEntity getGathering(Long gatheringId) {
        GatheringEntity g = store.gatherings.get(gatheringId);
        if (g == null) throw new NoSuchElementException("모임을 찾을 수 없습니다.");
        return g;
    }

    private ApplicationEntity getApplication(Long applicationId) {
        ApplicationEntity a = store.applications.get(applicationId);
        if (a == null) throw new NoSuchElementException("신청 정보를 찾을 수 없습니다.");
        return a;
    }

    private TodoEntity getTodo(Long todoId) {
        TodoEntity t = store.todos.get(todoId);
        if (t == null) throw new NoSuchElementException("Todo를 찾을 수 없습니다.");
        return t;
    }

    private void validateLeader(GatheringEntity g) {
        if (!Objects.equals(g.leaderId, currentUserId())) {
            throw new SecurityException("모임장만 접근할 수 있습니다.");
        }
    }

    private void validateMember(Long gatheringId) {
        Set<Long> members = store.gatheringMembers.getOrDefault(gatheringId, Set.of());
        if (!members.contains(currentUserId())) {
            throw new SecurityException("참여 멤버만 접근할 수 있습니다.");
        }
    }
}
