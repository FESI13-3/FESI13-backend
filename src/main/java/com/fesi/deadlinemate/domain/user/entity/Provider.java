package com.fesi.deadlinemate.domain.user.entity;

import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;

public enum Provider {
    EMAIL,
    KAKAO,
    GOOGLE;

    public static Provider fromString(String value) {
        try {
            return Provider.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER,
                    "지원하지 않는 OAuth 제공자입니다: " + value);
        }
    }

    public boolean isOAuth() {
        return this != EMAIL;
    }
}
