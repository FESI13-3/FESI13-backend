package com.fesi.deadlinemate.domain.auth.event;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {

    private final Long userId;
    private final String email;
    private final String nickname;
    private final Provider provider;
}
