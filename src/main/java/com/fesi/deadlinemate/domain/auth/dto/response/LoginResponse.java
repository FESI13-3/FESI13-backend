package com.fesi.deadlinemate.domain.auth.dto.response;

import com.fesi.deadlinemate.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                UserInfo.from(user)
        );
    }

    @Getter
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
        private String nickname;
        private String profileImage;
        private java.math.BigDecimal reputationScore;

        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getProfileImage(),
                    user.getReputationScore()
            );
        }
    }
}
