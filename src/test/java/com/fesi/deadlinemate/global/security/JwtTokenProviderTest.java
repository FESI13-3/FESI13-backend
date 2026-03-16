package com.fesi.deadlinemate.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";
        long accessExpiration = 3600000;
        long refreshExpiration = 604800000;
        jwtTokenProvider = new JwtTokenProvider(secret, accessExpiration, refreshExpiration);
    }

    @Test
    @DisplayName("Access Token을 생성하고 정보를 추출할 수 있다")
    void generateAndParseAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com");

        assertNotNull(token);
        assertEquals(1L, jwtTokenProvider.getUserId(token));
        assertEquals("test@example.com", jwtTokenProvider.getEmail(token));
        assertEquals("ACCESS", jwtTokenProvider.getTokenType(token));
    }

    @Test
    @DisplayName("Refresh Token을 생성하고 정보를 추출할 수 있다")
    void generateAndParseRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(1L, "test@example.com");

        assertNotNull(token);
        assertEquals(1L, jwtTokenProvider.getUserId(token));
        assertEquals("REFRESH", jwtTokenProvider.getTokenType(token));
    }

    @Test
    @DisplayName("유효한 토큰은 검증에 성공한다")
    void validateValidToken() {
        String token = jwtTokenProvider.generateAccessToken(1L, "test@example.com");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("잘못된 토큰은 검증에 실패한다")
    void validateInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("null 토큰은 검증에 실패한다")
    void validateNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    @DisplayName("만료된 토큰은 검증에 실패한다")
    void validateExpiredToken() {
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                -1000, -1000);

        String token = shortLivedProvider.generateAccessToken(1L, "test@example.com");

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("다른 시크릿으로 서명된 토큰은 검증에 실패한다")
    void validateTokenWithDifferentSecret() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "other-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                3600000, 604800000);

        String token = otherProvider.generateAccessToken(1L, "test@example.com");

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
