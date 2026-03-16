package com.fesi.deadlinemate.domain.auth.service;

import com.fesi.deadlinemate.domain.auth.dto.request.RefreshRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.AvailabilityResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.TokenResponse;
import com.fesi.deadlinemate.domain.auth.entity.RefreshToken;
import com.fesi.deadlinemate.domain.auth.provider.EmailAuthProvider;
import com.fesi.deadlinemate.domain.auth.provider.OAuthAuthProvider;
import com.fesi.deadlinemate.domain.auth.repository.RefreshTokenRepository;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserService userService;
    @Mock
    private EmailAuthProvider emailAuthProvider;
    @Mock
    private OAuthAuthProvider oAuthAuthProvider;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("유효한 Refresh Token으로 새 토큰을 발급받는다")
    void refreshSuccess() {
        String oldRefreshToken = "old-refresh-token";
        RefreshRequest request = new RefreshRequest(oldRefreshToken);
        RefreshToken storedToken = RefreshToken.builder()
                .token(oldRefreshToken)
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getTokenType(oldRefreshToken)).willReturn("REFRESH");
        given(refreshTokenRepository.findByToken(oldRefreshToken)).willReturn(Optional.of(storedToken));
        given(jwtTokenProvider.getUserId(oldRefreshToken)).willReturn(1L);
        given(jwtTokenProvider.getEmail(oldRefreshToken)).willReturn("test@example.com");
        given(jwtTokenProvider.generateAccessToken(1L, "test@example.com")).willReturn("new-access");
        given(jwtTokenProvider.generateRefreshToken(1L, "test@example.com")).willReturn("new-refresh");
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(storedToken);

        TokenResponse response = authService.refresh(request);

        assertEquals("new-access", response.getAccessToken());
        assertEquals("new-refresh", response.getRefreshToken());
        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 refresh 시 예외가 발생한다")
    void refreshInvalidToken() {
        given(jwtTokenProvider.validateToken("invalid")).willReturn(false);

        assertThrows(BusinessException.class,
                () -> authService.refresh(new RefreshRequest("invalid")));
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 가능한 경우")
    void checkEmailAvailable() {
        given(userService.existsByEmail("new@example.com")).willReturn(false);

        AvailabilityResponse response = authService.checkEmailAvailability("new@example.com");

        assertTrue(response.isAvailable());
    }

    @Test
    @DisplayName("이메일 중복 확인 - 이미 사용 중인 경우")
    void checkEmailNotAvailable() {
        given(userService.existsByEmail("exists@example.com")).willReturn(true);

        AvailabilityResponse response = authService.checkEmailAvailability("exists@example.com");

        assertFalse(response.isAvailable());
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 사용 가능한 경우")
    void checkNicknameAvailable() {
        given(userService.existsByNickname("새닉네임")).willReturn(false);

        AvailabilityResponse response = authService.checkNicknameAvailability("새닉네임");

        assertTrue(response.isAvailable());
    }
}
