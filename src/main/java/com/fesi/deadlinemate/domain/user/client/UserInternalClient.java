package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.repository.UserRepository;
import com.fesi.deadlinemate.domain.user.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInternalClient implements UserClient {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    public UserInfo findById(Long userId) {
        User user = userService.findById(userId);
        return UserInfo.from(user);
    }

    @Override
    public Map<Long, UserInfo> findByIds(List<Long> userIds) {
        return userRepository.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(User::getId, UserInfo::from));
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
