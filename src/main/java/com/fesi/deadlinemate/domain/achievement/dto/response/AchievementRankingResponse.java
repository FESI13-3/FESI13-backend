package com.fesi.deadlinemate.domain.achievement.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record AchievementRankingResponse(
        List<RankingItemResponse> ranking
) {
    public static AchievementRankingResponse of(List<RankingItemResponse> ranking) {
        return AchievementRankingResponse.builder()
                .ranking(ranking)
                .build();
    }

    @Builder
    public record RankingItemResponse(
            int rank,
            Long userId,
            String nickname,
            String profileImage,
            BigDecimal overallRate
    ) {
    }
}
