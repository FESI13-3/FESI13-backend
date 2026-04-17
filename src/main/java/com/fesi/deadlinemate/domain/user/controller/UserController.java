package com.fesi.deadlinemate.domain.user.controller;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import com.fesi.deadlinemate.domain.user.dto.request.ChangePasswordRequest;
import com.fesi.deadlinemate.domain.user.dto.request.UpdateProfileRequest;
import com.fesi.deadlinemate.global.common.ImageStorageService;
import com.fesi.deadlinemate.domain.user.dto.response.PublicProfileResponse;
import com.fesi.deadlinemate.domain.user.dto.response.UserProfileResponse;
import com.fesi.deadlinemate.domain.user.entity.User;
import com.fesi.deadlinemate.domain.user.service.UserService;
import com.fesi.deadlinemate.domain.user.service.UserStatsQueryService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserStatsQueryService userStatsQueryService;
    private final GatheringClient gatheringClient;
    private final ImageStorageService imageStorageService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userService.findById(userId);

        long completedGatherings = userStatsQueryService.countCompletedGatherings(userId);
        BigDecimal avgAchievementRate = userStatsQueryService.calculateAvgAchievementRate(userId);
        long reviewCount = userStatsQueryService.countReviews(userId);

        return ResponseEntity.ok(ApiResponse.success(
                UserProfileResponse.from(user, completedGatherings, avgAchievementRate, reviewCount)
        ));
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @RequestPart(value = "request", required = false) @Valid UpdateProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        Long userId = (Long) authentication.getPrincipal();

        String oldImageUrl = null;
        String imageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            oldImageUrl = userService.findById(userId).getProfileImage();
            imageUrl = imageStorageService.upload(profileImage, "profiles");
        } else if (request != null) {
            imageUrl = request.getProfileImage();
        }

        String nickname = (request != null) ? request.getNickname() : null;
        User user = userService.updateProfile(userId, nickname, imageUrl);

        if (oldImageUrl != null) {
            imageStorageService.delete(oldImageUrl);
        }

        long completedGatherings = userStatsQueryService.countCompletedGatherings(userId);
        BigDecimal avgAchievementRate = userStatsQueryService.calculateAvgAchievementRate(userId);
        long reviewCount = userStatsQueryService.countReviews(userId);

        return ResponseEntity.ok(ApiResponse.success(
                UserProfileResponse.from(user, completedGatherings, avgAchievementRate, reviewCount)
        ));
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
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                gatheringClient.getMyGatherings(userId, status, sort, page, limit)
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getUserProfile(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(ApiResponse.success(PublicProfileResponse.from(user)));
    }
}
