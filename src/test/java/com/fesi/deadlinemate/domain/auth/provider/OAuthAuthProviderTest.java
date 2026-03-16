package com.fesi.deadlinemate.domain.auth.provider;

import com.fesi.deadlinemate.domain.auth.client.OAuthClient;
import com.fesi.deadlinemate.domain.auth.client.OAuthClientFactory;
import com.fesi.deadlinemate.domain.auth.client.dto.OAuthUserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OAuthAuthProviderTest {

    @InjectMocks
    private OAuthAuthProvider oAuthAuthProvider;

    @Mock
    private OAuthClientFactory oAuthClientFactory;

    @Mock
    private UserService userService;

    @Mock
    private OAuthClient oAuthClient;

    @Test
    @DisplayName("신규 OAuth 사용자 인증 시 isNewUser가 true이다")
    void authenticateNewUser() {
        OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .providerId("12345")
                .email("kakao@kakao.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .build();

        User user = User.builder()
                .email("kakao@kakao.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .build();

        given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(oAuthClient);
        given(oAuthClient.getAccessToken("auth-code")).willReturn("access-token");
        given(oAuthClient.getUserInfo("access-token")).willReturn(userInfo);
        given(userService.existsByProviderAndProviderId(Provider.KAKAO, "12345")).willReturn(false);
        given(userService.findOrCreateOAuthUser(anyString(), anyString(), any(), eq(Provider.KAKAO), eq("12345")))
                .willReturn(user);

        OAuthAuthProvider.OAuthResult result = oAuthAuthProvider.authenticate(Provider.KAKAO, "auth-code");

        assertTrue(result.isNewUser());
        assertEquals("kakao@kakao.com", result.user().getEmail());
    }

    @Test
    @DisplayName("기존 OAuth 사용자 인증 시 isNewUser가 false이다")
    void authenticateExistingUser() {
        OAuthUserInfo userInfo = OAuthUserInfo.builder()
                .providerId("12345")
                .email("kakao@kakao.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .build();

        User user = User.builder()
                .email("kakao@kakao.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .build();

        given(oAuthClientFactory.getClient(Provider.KAKAO)).willReturn(oAuthClient);
        given(oAuthClient.getAccessToken("auth-code")).willReturn("access-token");
        given(oAuthClient.getUserInfo("access-token")).willReturn(userInfo);
        given(userService.existsByProviderAndProviderId(Provider.KAKAO, "12345")).willReturn(true);
        given(userService.findOrCreateOAuthUser(anyString(), anyString(), any(), eq(Provider.KAKAO), eq("12345")))
                .willReturn(user);

        OAuthAuthProvider.OAuthResult result = oAuthAuthProvider.authenticate(Provider.KAKAO, "auth-code");

        assertFalse(result.isNewUser());
    }
}
