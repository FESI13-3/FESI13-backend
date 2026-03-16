package com.fesi.deadlinemate.domain.auth.service;

import com.fesi.deadlinemate.global.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
    }

    @Test
    @DisplayName("5회 미만 실패 시 차단되지 않는다")
    void underMaxAttempts() {
        String email = "test@example.com";
        for (int i = 0; i < 4; i++) {
            loginAttemptService.recordFailure(email);
        }

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(email));
    }

    @Test
    @DisplayName("5회 연속 실패 시 차단된다")
    void blockedAfterMaxAttempts() {
        String email = "test@example.com";
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailure(email);
        }

        assertThrows(BusinessException.class, () -> loginAttemptService.checkBlocked(email));
    }

    @Test
    @DisplayName("실패 횟수 초기화 후 차단이 해제된다")
    void resetUnblocks() {
        String email = "test@example.com";
        for (int i = 0; i < 5; i++) {
            loginAttemptService.recordFailure(email);
        }

        loginAttemptService.resetAttempts(email);

        assertDoesNotThrow(() -> loginAttemptService.checkBlocked(email));
    }
}
