package com.fesi.deadlinemate.domain.auth.service;

import com.fesi.deadlinemate.domain.auth.dto.request.LoginRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.RefreshRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.SignupRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.*;
import com.fesi.deadlinemate.domain.auth.entity.RefreshToken;
import com.fesi.deadlinemate.domain.auth.event.UserLoggedInEvent;
import com.fesi.deadlinemate.domain.auth.event.UserRegisteredEvent;
import com.fesi.deadlinemate.domain.auth.provider.EmailAuthProvider;
import com.fesi.deadlinemate.domain.auth.provider.OAuthAuthProvider;
import com.fesi.deadlinemate.domain.auth.repository.RefreshTokenRepository;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final EmailAuthProvider emailAuthProvider;
    private final OAuthAuthProvider oAuthAuthProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        User user = userService.createEmailUser(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = createAndSaveRefreshToken(user);

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getId(), user.getEmail(), user.getNickname(), Provider.EMAIL));

        return SignupResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        loginAttemptService.checkBlocked(request.getEmail());

        try {
            User user = emailAuthProvider.authenticate(request.getEmail(), request.getPassword());
            loginAttemptService.resetAttempts(request.getEmail());

            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
            String refreshToken = createAndSaveRefreshToken(user);

            eventPublisher.publishEvent(new UserLoggedInEvent(user.getId(), Provider.EMAIL));

            return LoginResponse.of(user, accessToken, refreshToken);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.INVALID_CREDENTIALS) {
                loginAttemptService.recordFailure(request.getEmail());
            }
            throw e;
        }
    }

    @Transactional
    public OAuthCallbackResponse oauthCallback(Provider provider, String code, String redirectUri) {
        OAuthAuthProvider.OAuthResult result = oAuthAuthProvider.authenticate(provider, code, redirectUri);
        User user = result.user();
        boolean isNewUser = result.isNewUser();

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = createAndSaveRefreshToken(user);

        if (isNewUser) {
            eventPublisher.publishEvent(new UserRegisteredEvent(
                    user.getId(), user.getEmail(), user.getNickname(), provider));
        } else {
            eventPublisher.publishEvent(new UserLoggedInEvent(user.getId(), provider));
        }

        return OAuthCallbackResponse.of(accessToken, refreshToken, isNewUser);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!"REFRESH".equals(jwtTokenProvider.getTokenType(token))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(token);
        String email = jwtTokenProvider.getEmail(token);

        refreshTokenRepository.delete(storedToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, email);

        refreshTokenRepository.save(RefreshToken.builder()
                .token(newRefreshToken)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build());

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);
    }

    public AvailabilityResponse checkEmailAvailability(String email) {
        return AvailabilityResponse.of(!userService.existsByEmail(email));
    }

    public AvailabilityResponse checkNicknameAvailability(String nickname) {
        return AvailabilityResponse.of(!userService.existsByNickname(nickname));
    }

    private String createAndSaveRefreshToken(User user) {
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .userId(user.getId())
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build());
        return refreshToken;
    }
}
