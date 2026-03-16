package com.fesi.deadlinemate.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthCallbackResponse {

    private String accessToken;
    private String refreshToken;
    private boolean isNewUser;

    public static OAuthCallbackResponse of(String accessToken, String refreshToken, boolean isNewUser) {
        return new OAuthCallbackResponse(accessToken, refreshToken, isNewUser);
    }
}
