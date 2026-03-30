package com.fesi.deadlinemate.domain.user.client;

import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.math.BigDecimal;

public interface UserClient {

    UserInfo findById(Long userId);

    boolean existsById(Long userId);

    void addReputationScore(Long userId, BigDecimal delta);
}
