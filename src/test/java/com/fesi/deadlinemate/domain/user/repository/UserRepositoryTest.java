package com.fesi.deadlinemate.domain.user.repository;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.global.config.JpaConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
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

    @Test
    @DisplayName("ID 목록으로 사용자들을 조회할 수 있다")
    void findByIdIn() {
        User user1 = createEmailUser("user1@example.com", "유저1");
        User user2 = createEmailUser("user2@example.com", "유저2");
        User user3 = createEmailUser("user3@example.com", "유저3");

        User saved1 = userRepository.save(user1);
        User saved2 = userRepository.save(user2);
        userRepository.save(user3);

        List<User> found = userRepository.findByIdIn(List.of(saved1.getId(), saved2.getId()));

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(user -> user.getEmail().equals("user1@example.com")));
        assertTrue(found.stream().anyMatch(user -> user.getEmail().equals("user2@example.com")));
    }

    @Test
    @DisplayName("ID 목록에 해당하는 사용자가 없으면 빈 리스트를 반환한다")
    void findByIdInEmpty() {
        List<User> found = userRepository.findByIdIn(List.of(999L, 1000L));

        assertTrue(found.isEmpty());
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
