package com.fesi.deadlinemate.domain.auth.client;

import com.fesi.deadlinemate.domain.auth.client.dto.OAuthUserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OAuthClientFactoryTest {

    private OAuthClientFactory factory;

    @BeforeEach
    void setUp() {
        OAuthClient kakaoClient = new OAuthClient() {
            @Override
            public Provider getProvider() { return Provider.KAKAO; }
            @Override
            public String getAccessToken(String code) { return "token"; }
            @Override
            public OAuthUserInfo getUserInfo(String accessToken) { return null; }
        };

        OAuthClient googleClient = new OAuthClient() {
            @Override
            public Provider getProvider() { return Provider.GOOGLE; }
            @Override
            public String getAccessToken(String code) { return "token"; }
            @Override
            public OAuthUserInfo getUserInfo(String accessToken) { return null; }
        };

        factory = new OAuthClientFactory(List.of(kakaoClient, googleClient));
    }

    @Test
    @DisplayName("Provider에 맞는 OAuthClient를 반환한다")
    void getClientByProvider() {
        OAuthClient kakaoClient = factory.getClient(Provider.KAKAO);
        OAuthClient googleClient = factory.getClient(Provider.GOOGLE);

        assertEquals(Provider.KAKAO, kakaoClient.getProvider());
        assertEquals(Provider.GOOGLE, googleClient.getProvider());
    }

    @Test
    @DisplayName("지원하지 않는 Provider일 경우 예외를 던진다")
    void unsupportedProvider() {
        assertThrows(BusinessException.class, () -> factory.getClient(Provider.EMAIL));
    }
}
