package com.fesi.deadlinemate.domain.user.repository;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmail() {
        User user = createEmailUser("test@example.com", "테스터");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("테스터", found.get().getNickname());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional을 반환한다")
    void findByEmailNotFound() {
        Optional<User> found = userRepository.findByEmail("notexist@example.com");

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Provider와 ProviderId로 소셜 사용자를 조회할 수 있다")
    void findByProviderAndProviderId() {
        User user = User.builder()
                .email("kakao_12345@kakao.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByProviderAndProviderId(Provider.KAKAO, "12345");

        assertTrue(found.isPresent());
        assertEquals(Provider.KAKAO, found.get().getProvider());
        assertEquals("12345", found.get().getProviderId());
    }

    @Test
    @DisplayName("이메일 존재 여부를 확인할 수 있다")
    void existsByEmail() {
        User user = createEmailUser("exists@example.com", "존재유저");
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("exists@example.com"));
        assertFalse(userRepository.existsByEmail("notexists@example.com"));
    }

    @Test
    @DisplayName("닉네임 존재 여부를 확인할 수 있다")
    void existsByNickname() {
        User user = createEmailUser("test@example.com", "마감왕");
        userRepository.save(user);

        assertTrue(userRepository.existsByNickname("마감왕"));
        assertFalse(userRepository.existsByNickname("없는닉네임"));
    }

    @Test
    @DisplayName("사용자 생성 시 기본 평판 점수는 36.5이다")
    void defaultReputationScore() {
        User user = createEmailUser("test@example.com", "테스터");
        User saved = userRepository.save(user);

        assertEquals(0, BigDecimal.valueOf(36.5).compareTo(saved.getReputationScore()));
        assertTrue(saved.isActive());
    }

    @Test
    @DisplayName("이메일 사용자인지 확인할 수 있다")
    void isEmailUser() {
        User emailUser = createEmailUser("email@example.com", "이메일유저");
        User kakaoUser = User.builder()
                .email("kakao@kakao.com")
                .nickname("카카오유저")
                .provider(Provider.KAKAO)
                .providerId("kakao123")
                .build();

        assertTrue(emailUser.isEmailUser());
        assertFalse(kakaoUser.isEmailUser());
    }

    private User createEmailUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .passwordHash("$2a$10$hashedpassword")
                .nickname(nickname)
                .provider(Provider.EMAIL)
                .build();
    }
}
