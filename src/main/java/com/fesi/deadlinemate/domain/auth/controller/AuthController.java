package com.fesi.deadlinemate.domain.auth.controller;

import com.fesi.deadlinemate.domain.auth.dto.request.LoginRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.SignupRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.LoginResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.OAuthCallbackResponse;
import com.fesi.deadlinemate.domain.auth.dto.response.SignupResponse;
import com.fesi.deadlinemate.domain.auth.service.AuthService;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<SignupResponse>> register(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResponse<OAuthCallbackResponse>> kakaoCallback(@RequestParam String code) {
        OAuthCallbackResponse response = authService.oauthCallback(Provider.KAKAO, code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<OAuthCallbackResponse>> googleCallback(@RequestParam String code) {
        OAuthCallbackResponse response = authService.oauthCallback(Provider.GOOGLE, code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
