package com.fesi.deadlinemate.domain.auth.dto.response;

import com.fesi.deadlinemate.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long userId;
    private String email;
    private String nickname;
    private String accessToken;
    private String refreshToken;

    public static SignupResponse of(User user, String accessToken, String refreshToken) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                accessToken,
                refreshToken
        );
    }
}
