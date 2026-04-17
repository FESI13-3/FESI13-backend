package com.fesi.deadlinemate.domain.user.dto.response;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private Provider provider;
    private BigDecimal reputationScore;
    private String reputationLabel;
    private long completedGatherings;
    private BigDecimal avgAchievementRate;
    private long reviewCount;

    public static UserProfileResponse from(User user,
                                           long completedGatherings,
                                           BigDecimal avgAchievementRate,
                                           long reviewCount) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .reputationScore(user.getReputationScore())
                .reputationLabel(getReputationLabel(user.getReputationScore()))
                .completedGatherings(completedGatherings)
                .avgAchievementRate(avgAchievementRate)
                .reviewCount(reviewCount)
                .build();
    }

    private static String getReputationLabel(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) return "최고 메이트";
        if (score.compareTo(BigDecimal.valueOf(35)) >= 0) return "신뢰 메이트";
        if (score.compareTo(BigDecimal.valueOf(30)) >= 0) return "성장 메이트";
        return "새싹 메이트";
    }
}
