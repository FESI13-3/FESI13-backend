package com.fesi.deadlinemate.domain.auth.event;

import com.fesi.deadlinemate.domain.user.entity.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoggedInEvent {

    private final Long userId;
    private final Provider provider;
}
