package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInternalClient implements UserClient {

    private final UserService userService;

    @Override
    public UserInfo findById(Long userId) {
        User user = userService.findById(userId);
        return UserInfo.from(user);
    }

    @Override
    public boolean existsById(Long userId) {
        try {
            userService.findById(userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
