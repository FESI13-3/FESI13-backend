package com.fesi.deadlinemate.domain.gathering.dto.mock;

import java.util.List;

public class AchievementDtos {
    public record WeeklyRateDto(
            Integer week,
            Double rate
    ) {}

    public record MemberAchievementDto(
            Long userId,
            String nickname,
            List<WeeklyRateDto> weeklyRates,
            Double overallRate
    ) {}

    public record AchievementResponse(
            List<MemberAchievementDto> members,
            List<WeeklyRateDto> teamWeeklyRates,
            Double teamOverallRate
    ) {}

    public record RankingItemDto(
            Integer rank,
            Long userId,
            String nickname,
            String profileImage,
            Double overallRate
    ) {}

    public record RankingResponse(
            List<RankingItemDto> ranking
    ) {}
}
