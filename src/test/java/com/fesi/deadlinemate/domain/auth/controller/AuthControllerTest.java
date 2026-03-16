package com.fesi.deadlinemate.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesi.deadlinemate.domain.auth.dto.request.LoginRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.SignupRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.LoginResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.SignupResponse;
import com.fesi.deadlinemate.domain.auth.service.AuthService;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import com.fesi.deadlinemate.global.error.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 시 201과 토큰을 반환한다")
    void registerSuccess() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "P@ssw0rd1!", "마감왕");
        SignupResponse response = new SignupResponse(1L, "test@example.com", "마감왕",
                "access-token", "refresh-token");

        given(authService.signup(any(SignupRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("이메일 중복 시 409를 반환한다")
    void registerEmailConflict() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "P@ssw0rd1!", "마감왕");

        given(authService.signup(any(SignupRequest.class)))
                .willThrow(new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 회원가입 시 400을 반환한다")
    void registerInvalidEmail() throws Exception {
        SignupRequest request = new SignupRequest("invalid-email", "P@ssw0rd1!", "마감왕");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 성공 시 200과 토큰/유저정보를 반환한다")
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "P@ssw0rd1!");
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                1L, "test@example.com", "마감왕", null, BigDecimal.valueOf(36.5));
        LoginResponse response = new LoginResponse("access-token", "refresh-token", userInfo);

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.user.nickname").value("마감왕"));
    }

    @Test
    @DisplayName("로그인 실패 시 401을 반환한다")
    void loginInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }
}
