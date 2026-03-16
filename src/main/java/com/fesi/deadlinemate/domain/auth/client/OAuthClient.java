package com.fesi.deadlinemate.domain.auth.client;

import com.fesi.deadlinemate.domain.auth.client.dto.OAuthUserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;

public interface OAuthClient {

    Provider getProvider();

    String getAccessToken(String code);

    OAuthUserInfo getUserInfo(String accessToken);
}
