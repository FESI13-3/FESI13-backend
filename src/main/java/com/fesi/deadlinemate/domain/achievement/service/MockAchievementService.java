package com.fesi.deadlinemate.domain.achievement.service;

import com.fesi.deadlinemate.domain.achievement.dto.MockAchievementDtos.AchievementResponse;
import com.fesi.deadlinemate.domain.achievement.dto.MockAchievementDtos.MemberAchievementDto;
import com.fesi.deadlinemate.domain.achievement.dto.MockAchievementDtos.RankingItemDto;
import com.fesi.deadlinemate.domain.achievement.dto.MockAchievementDtos.RankingResponse;
import com.fesi.deadlinemate.domain.achievement.dto.MockAchievementDtos.WeeklyRateDto;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAchievementCalculator;
import com.fesi.deadlinemate.global.mock.support.MockPermissionService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MockAchievementService {
    private final MockStore store;
    private final MockPermissionService permissionService;
    private final MockAchievementCalculator calculator;

    public MockAchievementService(
            MockStore store,
            MockPermissionService permissionService,
            MockAchievementCalculator calculator
    ) {
        this.store = store;
        this.permissionService = permissionService;
        this.calculator = calculator;
    }

    public AchievementResponse getAchievements(Long gatheringId) {
        permissionService.validateMember(gatheringId);

        Set<Long> members = store.gatheringMembers.getOrDefault(gatheringId, Set.of());

        List<MemberAchievementDto> memberAchievements = members.stream()
                .map(userId -> {
                    List<MockTodoEntity> userTodos = store.todos.values().stream()
                            .filter(t -> t.gatheringId.equals(gatheringId))
                            .filter(t -> t.userId.equals(userId))
                            .toList();

                    Map<Integer, List<MockTodoEntity>> byWeek = userTodos.stream()
                            .collect(Collectors.groupingBy(t -> t.week));

                    List<WeeklyRateDto> weeklyRates = byWeek.entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(e -> new WeeklyRateDto(e.getKey(), calculator.calcRate(e.getValue())))
                            .toList();

                    return new MemberAchievementDto(
                            userId,
                            "유저" + userId,
                            weeklyRates,
                            calculator.calcRate(userTodos)
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

        return new AchievementResponse(
                memberAchievements,
                teamWeeklyRates,
                calculator.round(teamOverall)
        );
    }

    public RankingResponse getAchievementRanking(Long gatheringId) {
        permissionService.validateMember(gatheringId);

        List<RankingItemDto> ranking = store.gatheringMembers.getOrDefault(gatheringId, Set.of()).stream()
                .map(userId -> {
                    List<MockTodoEntity> userTodos = store.todos.values().stream()
                            .filter(t -> t.gatheringId.equals(gatheringId))
                            .filter(t -> t.userId.equals(userId))
                            .toList();

                    return new RankingItemDto(
                            0,
                            userId,
                            "유저" + userId,
                            "https://example.com/profile.png",
                            calculator.calcRate(userTodos)
                    );
                })
                .sorted(Comparator.comparing(RankingItemDto::overallRate).reversed())
                .toList();

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
}
