package com.fesi.deadlinemate.domain.gathering.dto.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportDtos {
    public record ReportGatheringDto(
            String title,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public record MemberResultDto(
            Long userId,
            String nickname,
            Double overallRate,
            Integer longestStreak,
            Integer completedTodos,
            Integer totalTodos,
            List<Double> weeklyRates
    ) {}

    public record ReportResponse(
            ReportGatheringDto gathering,
            Double teamOverallRate,
            List<Map<String, Object>> weeklyRates,
            List<MemberResultDto> memberResults,
            Map<String, Object> awards
    ) {}
}
