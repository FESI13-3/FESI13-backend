package com.fesi.deadlinemate.domain.auth.client.google;

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
public class GoogleOAuthClient implements OAuthClient {

    private final RestTemplate restTemplate;

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.google.token-url}")
    private String tokenUrl;

    @Value("${oauth.google.user-info-url}")
    private String userInfoUrl;

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
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
            log.error("Failed to get Google access token", e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            Map<String, Object> body = response.getBody();
            String providerId = (String) body.get("id");
            String email = (String) body.get("email");
            String nickname = (String) body.get("name");
            String profileImage = (String) body.get("picture");

            if (nickname == null) {
                nickname = "구글유저";
            }

            return OAuthUserInfo.builder()
                    .providerId(providerId)
                    .email(email)
                    .nickname(nickname)
                    .profileImage(profileImage)
                    .provider(Provider.GOOGLE)
                    .build();
        } catch (RestClientException e) {
            log.error("Failed to get Google user info", e);
            throw new BusinessException(ErrorCode.OAUTH_AUTHENTICATION_FAILED);
        }
    }
}
