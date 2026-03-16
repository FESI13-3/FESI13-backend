package com.fesi.deadlinemate.global.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private final boolean success;
    private final Object data;
    private final String message;
    private final String errorCode;

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(false, null, errorCode.getMessage(), errorCode.name());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(false, null, message, errorCode.name());
    }
}
