package com.fesi.deadlinemate.domain.auth.client.dto;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OAuthUserInfo {

    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImage;
    private final Provider provider;
}
