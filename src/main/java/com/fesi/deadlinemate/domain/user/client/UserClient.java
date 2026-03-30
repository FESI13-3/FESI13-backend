package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.util.List;
import java.util.Map;

public interface UserClient {

    UserInfo findById(Long userId);

    Map<Long, UserInfo> findByIds(List<Long> userIds);

    boolean existsById(Long userId);
}
