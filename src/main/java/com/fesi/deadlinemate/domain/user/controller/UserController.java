package com.fesi.deadlinemate.domain.user.controller;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import com.fesi.deadlinemate.domain.user.dto.request.ChangePasswordRequest;
import com.fesi.deadlinemate.domain.user.dto.request.UpdateProfileRequest;
import com.fesi.deadlinemate.domain.user.dto.response.PublicProfileResponse;
import com.fesi.deadlinemate.domain.user.dto.response.UserProfileResponse;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GatheringClient gatheringClient;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.findById(userId);
        return ResponseEntity.ok(ApiResponse.success(UserProfileResponse.from(user)));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.updateProfile(userId, request.getNickname(), request.getProfileImage());
        return ResponseEntity.ok(ApiResponse.success(UserProfileResponse.from(user)));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deactivate(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/me/gatherings")
    public ResponseEntity<ApiResponse<MyGatheringListResponse>> getMyGatherings(
            Authentication authentication,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                gatheringClient.getMyGatherings(userId, status, page, limit)
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getUserProfile(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(ApiResponse.success(PublicProfileResponse.from(user)));
    }
}
