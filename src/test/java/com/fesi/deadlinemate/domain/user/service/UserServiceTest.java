package com.fesi.deadlinemate.domain.user.service;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("이메일 사용자를 생성할 수 있다")
    void createEmailUser() {
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userRepository.existsByNickname("마감왕")).willReturn(false);
        given(passwordEncoder.encode("password")).willReturn("encoded");
        given(userRepository.save(any(User.class))).willAnswer(i -> i.getArgument(0));

        User user = userService.createEmailUser("test@example.com", "password", "마감왕");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("마감왕", user.getNickname());
        assertEquals(Provider.EMAIL, user.getProvider());
    }

    @Test
    @DisplayName("중복된 이메일로 생성 시 예외가 발생한다")
    void createEmailUserDuplicateEmail() {
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.createEmailUser("test@example.com", "password", "마감왕"));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("프로필을 업데이트할 수 있다")
    void updateProfile() {
        User user = createTestUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("새닉네임")).willReturn(false);

        User updated = userService.updateProfile(1L, "새닉네임", "https://new-image.jpg");

        assertEquals("새닉네임", updated.getNickname());
        assertEquals("https://new-image.jpg", updated.getProfileImage());
    }

    @Test
    @DisplayName("비밀번호를 변경할 수 있다")
    void changePassword() {
        User user = createTestUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("oldpass", user.getPasswordHash())).willReturn(true);
        given(passwordEncoder.encode("newpass")).willReturn("new-encoded");

        assertDoesNotThrow(() -> userService.changePassword(1L, "oldpass", "newpass"));
    }

    @Test
    @DisplayName("소셜 사용자가 비밀번호 변경 시 예외가 발생한다")
    void changePasswordSocialUser() {
        User socialUser = User.builder()
                .email("kakao@kakao.com")
                .nickname("카카오")
                .provider(Provider.KAKAO)
                .providerId("123")
                .build();
        setId(socialUser, 1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(socialUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.changePassword(1L, "old", "new"));

        assertEquals(ErrorCode.SOCIAL_USER_PASSWORD_CHANGE, ex.getErrorCode());
    }

    @Test
    @DisplayName("회원 탈퇴 시 비활성화된다")
    void deactivate() {
        User user = createTestUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        userService.deactivate(1L);

        assertFalse(user.isActive());
    }

    private User createTestUser(Long id) {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("$2a$10$hash")
                .nickname("마감왕")
                .provider(Provider.EMAIL)
                .build();
        setId(user, id);
        return user;
    }

    private void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
