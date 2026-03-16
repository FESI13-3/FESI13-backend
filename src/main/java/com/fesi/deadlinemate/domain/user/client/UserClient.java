package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;

public interface UserClient {

    UserInfo findById(Long userId);

    boolean existsById(Long userId);
}
