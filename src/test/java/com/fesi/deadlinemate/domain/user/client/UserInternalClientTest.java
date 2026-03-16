package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
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

    private User createUser(Long id) {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("$2a$10$hash")
                .nickname("마감왕")
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
