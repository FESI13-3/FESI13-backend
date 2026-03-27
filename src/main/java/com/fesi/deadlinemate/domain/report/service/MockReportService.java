package com.fesi.deadlinemate.domain.report.service;

import com.fesi.deadlinemate.domain.gathering.entity.MockGatheringEntity;
import com.fesi.deadlinemate.domain.report.dto.MockReportDtos.MemberResultDto;
import com.fesi.deadlinemate.domain.report.dto.MockReportDtos.ReportGatheringDto;
import com.fesi.deadlinemate.domain.report.dto.MockReportDtos.ReportResponse;
import com.fesi.deadlinemate.domain.todo.entity.MockTodoEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAchievementCalculator;
import com.fesi.deadlinemate.global.mock.support.MockFinder;
import com.fesi.deadlinemate.global.mock.support.MockPermissionService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MockReportService {
    private final MockStore store;
    private final MockFinder finder;
    private final MockPermissionService permissionService;
    private final MockAchievementCalculator calculator;

    public MockReportService(
            MockStore store,
            MockFinder finder,
            MockPermissionService permissionService,
            MockAchievementCalculator calculator
    ) {
        this.store = store;
        this.finder = finder;
        this.permissionService = permissionService;
        this.calculator = calculator;
    }

    public ReportResponse getReport(Long gatheringId) {
        permissionService.validateMember(gatheringId);

        MockGatheringEntity gathering = finder.getGathering(gatheringId);

        List<MemberResultDto> memberResults = store.gatheringMembers.getOrDefault(gatheringId, Set.of()).stream()
                .map(userId -> {
                    List<MockTodoEntity> userTodos = store.todos.values().stream()
                            .filter(t -> t.gatheringId.equals(gatheringId))
                            .filter(t -> t.userId.equals(userId))
                            .toList();

                    int completed = (int) userTodos.stream()
                            .filter(t -> Boolean.TRUE.equals(t.isCompleted))
                            .count();

                    int total = userTodos.size();

                    return new MemberResultDto(
                            userId,
                            "유저" + userId,
                            calculator.calcRate(userTodos),
                            completed,
                            completed,
                            total,
                            List.of(100.0, 80.0, 70.0, 90.0)
                    );
                })
                .toList();

        return new ReportResponse(
                new ReportGatheringDto(gathering.title, gathering.startDate, gathering.endDate),
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
}
