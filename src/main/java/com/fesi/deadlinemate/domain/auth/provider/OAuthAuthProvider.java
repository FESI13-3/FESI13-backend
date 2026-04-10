package com.fesi.deadlinemate.domain.auth.provider;

import com.fesi.deadlinemate.domain.auth.client.OAuthClient;
import com.fesi.deadlinemate.domain.auth.client.OAuthClientFactory;
import com.fesi.deadlinemate.domain.auth.client.dto.OAuthUserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthAuthProvider {

    private final OAuthClientFactory oAuthClientFactory;
    private final UserService userService;

    public OAuthResult authenticate(Provider provider, String code, String redirectUri) {
        OAuthClient client = oAuthClientFactory.getClient(provider);
        String accessToken = client.getAccessToken(code, redirectUri);
        OAuthUserInfo userInfo = client.getUserInfo(accessToken);

        boolean isNewUser = !userService.existsByProviderAndProviderId(provider, userInfo.getProviderId());

        User user = userService.findOrCreateOAuthUser(
                userInfo.getEmail(),
                userInfo.getNickname(),
                userInfo.getProfileImage(),
                provider,
                userInfo.getProviderId()
        );

        return new OAuthResult(user, isNewUser);
    }

    public record OAuthResult(User user, boolean isNewUser) {}
}
