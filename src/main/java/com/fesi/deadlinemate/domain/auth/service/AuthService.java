package com.fesi.deadlinemate.domain.auth.service;

import com.fesi.deadlinemate.domain.auth.dto.request.LoginRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.SignupRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.LoginResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.OAuthCallbackResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.SignupResponse;
import com.fesi.deadlinemate.domain.auth.event.UserLoggedInEvent;
import com.fesi.deadlinemate.domain.auth.event.UserRegisteredEvent;
import com.fesi.deadlinemate.domain.auth.provider.EmailAuthProvider;
import com.fesi.deadlinemate.domain.auth.provider.OAuthAuthProvider;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final EmailAuthProvider emailAuthProvider;
    private final OAuthAuthProvider oAuthAuthProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        User user = userService.createEmailUser(
                request.getEmail(),
                request.getPassword(),
                request.getNickname()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        eventPublisher.publishEvent(new UserRegisteredEvent(
                user.getId(), user.getEmail(), user.getNickname(), Provider.EMAIL));

        return SignupResponse.of(user, accessToken, refreshToken);
    }

    public LoginResponse login(LoginRequest request) {
        User user = emailAuthProvider.authenticate(request.getEmail(), request.getPassword());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        eventPublisher.publishEvent(new UserLoggedInEvent(user.getId(), Provider.EMAIL));

        return LoginResponse.of(user, accessToken, refreshToken);
    }

    @Transactional
    public OAuthCallbackResponse oauthCallback(Provider provider, String code) {
        OAuthAuthProvider.OAuthResult result = oAuthAuthProvider.authenticate(provider, code);
        User user = result.user();
        boolean isNewUser = result.isNewUser();

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        if (isNewUser) {
            eventPublisher.publishEvent(new UserRegisteredEvent(
                    user.getId(), user.getEmail(), user.getNickname(), provider));
        } else {
            eventPublisher.publishEvent(new UserLoggedInEvent(user.getId(), provider));
        }

        return OAuthCallbackResponse.of(accessToken, refreshToken, isNewUser);
    }
}
