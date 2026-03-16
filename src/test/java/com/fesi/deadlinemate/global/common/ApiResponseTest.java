package com.fesi.deadlinemate.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    @DisplayName("성공 응답에 데이터를 포함할 수 있다")
    void successWithData() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertTrue(response.isSuccess());
        assertEquals("hello", response.getData());
        assertNull(response.getMessage());
    }

    @Test
    @DisplayName("데이터 없이 성공 응답을 생성할 수 있다")
    void successWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("에러 응답에 메시지를 포함할 수 있다")
    void errorWithMessage() {
        ApiResponse<Void> response = ApiResponse.error("에러 발생");

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals("에러 발생", response.getMessage());
    }
}
