package com.fesi.deadlinemate.domain.auth.client.kakao;

import com.fesi.deadlinemate.domain.auth.client.OAuthClient;
import com.fesi.deadlinemate.domain.auth.client.dto.OAuthUserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private final RestTemplate restTemplate;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.kakao.token-url}")
    private String tokenUrl;

    @Value("${oauth.kakao.user-info-url}")
    private String userInfoUrl;

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public String getAccessToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, new HttpEntity<>(params, headers), Map.class);

            return (String) response.getBody().get("access_token");
        } catch (RestClientException e) {
            log.error("Failed to get Kakao access token", e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            Map<String, Object> body = response.getBody();
            String providerId = String.valueOf(body.get("id"));

            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

            Map<String, Object> profile = kakaoAccount != null
                    ? (Map<String, Object>) kakaoAccount.get("profile") : null;
            String nickname = profile != null ? (String) profile.get("nickname") : "카카오유저";
            String profileImage = profile != null ? (String) profile.get("profile_image_url") : null;

            if (email == null) {
                email = "kakao_" + providerId + "@kakao.com";
            }

            return OAuthUserInfo.builder()
                    .providerId(providerId)
                    .email(email)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .provider(Provider.KAKAO)
                    .build();
        } catch (RestClientException e) {
            log.error("Failed to get Kakao user info", e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
        }
    }
}
