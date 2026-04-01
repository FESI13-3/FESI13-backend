package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserInternalClientTest {

    @InjectMocks
    private UserInternalClient userInternalClient;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("UserClient로 유저 정보를 조회할 수 있다")
    void findById() {
        User user = createUser(1L);
        given(userService.findById(1L)).willReturn(user);

        UserInfo userInfo = userInternalClient.findById(1L);

        assertEquals(1L, userInfo.getId());
        assertEquals("test@example.com", userInfo.getEmail());
        assertEquals("마감왕", userInfo.getNickname());
        assertEquals(Provider.EMAIL, userInfo.getProvider());
    }

    @Test
    @DisplayName("존재하는 유저면 true를 반환한다")
    void existsByIdTrue() {
        given(userService.findById(1L)).willReturn(createUser(1L));

        assertTrue(userInternalClient.existsById(1L));
    }

    @Test
    @DisplayName("존재하지 않는 유저면 false를 반환한다")
    void existsByIdFalse() {
        given(userService.findById(999L))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertFalse(userInternalClient.existsById(999L));
    }

    @Test
    @DisplayName("여러 유저를 한번에 조회해 id를 key로 하는 Map으로 반환한다")
    void findByIds() {
        User user1 = createUser(1L, "user1@example.com", "마감왕1");
        User user2 = createUser(2L, "user2@example.com", "마감왕2");

        given(userRepository.findByIdIn(List.of(1L, 2L)))
                .willReturn(List.of(user1, user2));

        Map<Long, UserInfo> result = userInternalClient.findByIds(List.of(1L, 2L));

        assertEquals(2, result.size());

        UserInfo userInfo1 = result.get(1L);
        assertNotNull(userInfo1);
        assertEquals(1L, userInfo1.getId());
        assertEquals("user1@example.com", userInfo1.getEmail());
        assertEquals("마감왕1", userInfo1.getNickname());
        assertEquals(Provider.EMAIL, userInfo1.getProvider());

        UserInfo userInfo2 = result.get(2L);
        assertNotNull(userInfo2);
        assertEquals(2L, userInfo2.getId());
        assertEquals("user2@example.com", userInfo2.getEmail());
        assertEquals("마감왕2", userInfo2.getNickname());
        assertEquals(Provider.EMAIL, userInfo2.getProvider());
    }

    @Test
    @DisplayName("유저가 없으면 빈 Map을 반환한다")
    void findByIdsEmpty() {
        given(userRepository.findByIdIn(List.of(1L, 2L)))
                .willReturn(List.of());

        Map<Long, UserInfo> result = userInternalClient.findByIds(List.of(1L, 2L));

        assertTrue(result.isEmpty());
    }

    private User createUser(Long id) {
        return createUser(id, "test@example.com", "마감왕");
    }

    private User createUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .passwordHash("$2a$10$hash")
                .nickname(nickname)
                .provider(Provider.EMAIL)
                .build();
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
