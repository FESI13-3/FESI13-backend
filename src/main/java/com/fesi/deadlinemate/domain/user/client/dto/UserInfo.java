package com.fesi.deadlinemate.domain.user.client.dto;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class UserInfo {

    private final Long id;
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final Provider provider;
    private final BigDecimal reputationScore;
    private final boolean active;

    public static UserInfo from(User user) {
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .provider(user.getProvider())
                .reputationScore(user.getReputationScore())
                .active(user.isActive())
                .build();
    }
}
