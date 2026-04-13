package com.fesi.deadlinemate.domain.auth.controller;

import com.fesi.deadlinemate.domain.auth.dto.request.LoginRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.OAuthCallbackRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.RefreshRequest;
import com.fesi.deadlinemate.domain.auth.dto.request.SignupRequest;
import com.fesi.deadlinemate.domain.auth.dto.response.*;
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

    @PostMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<OAuthCallbackResponse>> oauthCallback(
            @PathVariable String provider,
            @RequestBody OAuthCallbackRequest request) {
        Provider oauthProvider = Provider.fromString(provider);
        OAuthCallbackResponse response = authService.oauthCallback(oauthProvider, request.getCode(), request.getRedirectUri());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmail(@RequestParam String email) {
        AvailabilityResponse response = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/check/nickname")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNickname(@RequestParam String nickname) {
        AvailabilityResponse response = authService.checkNicknameAvailability(nickname);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
