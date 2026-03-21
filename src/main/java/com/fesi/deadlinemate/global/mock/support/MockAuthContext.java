package com.fesi.deadlinemate.global.mock.support;

import org.springframework.stereotype.Component;

@Component
public class MockAuthContext {
    public Long currentUserId() {
        return 1L;
    }
}
