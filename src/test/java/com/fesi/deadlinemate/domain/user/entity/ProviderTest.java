package com.fesi.deadlinemate.domain.user.entity;

import com.fesi.deadlinemate.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ProviderTest {

    @ParameterizedTest
    @ValueSource(strings = {"kakao", "KAKAO", "Kakao"})
    @DisplayName("문자열로 Provider를 찾을 수 있다 (대소문자 무관)")
    void fromStringCaseInsensitive(String input) {
        assertEquals(Provider.KAKAO, Provider.fromString(input));
    }

    @Test
    @DisplayName("google 문자열로 GOOGLE Provider를 찾을 수 있다")
    void fromStringGoogle() {
        assertEquals(Provider.GOOGLE, Provider.fromString("google"));
    }

    @Test
    @DisplayName("지원하지 않는 provider 문자열은 예외를 던진다")
    void fromStringUnsupported() {
        assertThrows(BusinessException.class, () -> Provider.fromString("naver"));
    }

    @Test
    @DisplayName("OAuth provider인지 구분할 수 있다")
    void isOAuth() {
        assertFalse(Provider.EMAIL.isOAuth());
        assertTrue(Provider.KAKAO.isOAuth());
        assertTrue(Provider.GOOGLE.isOAuth());
    }
}
