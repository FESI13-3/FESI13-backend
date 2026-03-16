package com.fesi.deadlinemate.domain.user.dto.response;

import com.fesi.deadlinemate.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PublicProfileResponse {

    private Long id;
    private String nickname;
    private String profileImage;
    private BigDecimal reputationScore;
    private String reputationLabel;

    public static PublicProfileResponse from(User user) {
        return new PublicProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImage(),
                user.getReputationScore(),
                getReputationLabel(user.getReputationScore())
        );
    }

    private static String getReputationLabel(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(40)) >= 0) return "최고 메이트";
        if (score.compareTo(BigDecimal.valueOf(35)) >= 0) return "신뢰 메이트";
        if (score.compareTo(BigDecimal.valueOf(30)) >= 0) return "성장 메이트";
        return "새싹 메이트";
    }
}
