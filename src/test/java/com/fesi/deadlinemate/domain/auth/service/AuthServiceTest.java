package com.fesi.deadlinemate.domain.auth.service;

import com.fesi.deadlinemate.domain.auth.dto.request.LoginRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.SignupRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.LoginResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.SignupResponse;
import com.fesi.deadlinemate.domain.auth.event.UserLoggedInEvent;
import com.fesi.deadlinemate.domain.auth.event.UserRegisteredEvent;
import com.fesi.deadlinemate.domain.auth.provider.EmailAuthProvider;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private EmailAuthProvider emailAuthProvider;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("이메일 회원가입에 성공하면 토큰과 사용자 정보를 반환한다")
    void signupSuccess() {
        SignupRequest request = new SignupRequest("test@example.com", "P@ssw0rd1!", "마감왕");
        User user = createUser(1L, "test@example.com", "마감왕");

        given(userService.createEmailUser("test@example.com", "P@ssw0rd1!", "마감왕"))
                .willReturn(user);
        given(jwtTokenProvider.generateAccessToken(1L, "test@example.com"))
                .willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(1L, "test@example.com"))
                .willReturn("refresh-token");

        SignupResponse response = authService.signup(request);

        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("마감왕", response.getNickname());
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("회원가입 시 UserRegisteredEvent를 발행한다")
    void signupPublishesEvent() {
        SignupRequest request = new SignupRequest("test@example.com", "P@ssw0rd1!", "마감왕");
        User user = createUser(1L, "test@example.com", "마감왕");

        given(userService.createEmailUser(anyString(), anyString(), anyString())).willReturn(user);
        given(jwtTokenProvider.generateAccessToken(anyLong(), anyString())).willReturn("token");
        given(jwtTokenProvider.generateRefreshToken(anyLong(), anyString())).willReturn("token");

        authService.signup(request);

        ArgumentCaptor<UserRegisteredEvent> captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        UserRegisteredEvent event = captor.getValue();
        assertEquals(1L, event.getUserId());
        assertEquals("test@example.com", event.getEmail());
        assertEquals(Provider.EMAIL, event.getProvider());
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰과 사용자 정보를 반환한다")
    void loginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "P@ssw0rd1!");
        User user = createUser(1L, "test@example.com", "마감왕");

        given(emailAuthProvider.authenticate("test@example.com", "P@ssw0rd1!"))
                .willReturn(user);
        given(jwtTokenProvider.generateAccessToken(1L, "test@example.com"))
                .willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(1L, "test@example.com"))
                .willReturn("refresh-token");

        LoginResponse response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals(1L, response.getUser().getId());
        assertEquals("마감왕", response.getUser().getNickname());
    }

    @Test
    @DisplayName("로그인 시 UserLoggedInEvent를 발행한다")
    void loginPublishesEvent() {
        LoginRequest request = new LoginRequest("test@example.com", "P@ssw0rd1!");
        User user = createUser(1L, "test@example.com", "마감왕");

        given(emailAuthProvider.authenticate(anyString(), anyString())).willReturn(user);
        given(jwtTokenProvider.generateAccessToken(anyLong(), anyString())).willReturn("token");
        given(jwtTokenProvider.generateRefreshToken(anyLong(), anyString())).willReturn("token");

        authService.login(request);

        ArgumentCaptor<UserLoggedInEvent> captor = ArgumentCaptor.forClass(UserLoggedInEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(Provider.EMAIL, captor.getValue().getProvider());
    }

    private User createUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .passwordHash("$2a$10$hash")
                .nickname(nickname)
                .provider(Provider.EMAIL)
                .build();
        // Reflection to set ID since it's generated
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }
}
