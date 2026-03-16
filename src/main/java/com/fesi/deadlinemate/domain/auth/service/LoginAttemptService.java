package com.fesi.deadlinemate.domain.auth.service;

import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_SECONDS = 30;

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void checkBlocked(String email) {
        AttemptInfo info = attempts.get(email);
        if (info != null && info.isBlocked()) {
            throw new BusinessException(ErrorCode.LOGIN_ATTEMPTS_EXCEEDED);
        }
    }

    public void recordFailure(String email) {
        attempts.compute(email, (key, info) -> {
            if (info == null || info.isExpired()) {
                return new AttemptInfo(1, LocalDateTime.now().plusSeconds(LOCK_SECONDS));
            }
            return new AttemptInfo(info.count + 1, LocalDateTime.now().plusSeconds(LOCK_SECONDS));
        });
    }

    public void resetAttempts(String email) {
        attempts.remove(email);
    }

    private record AttemptInfo(int count, LocalDateTime lockUntil) {
        boolean isBlocked() {
            return count >= MAX_ATTEMPTS && !isExpired();
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(lockUntil);
        }
    }
}
