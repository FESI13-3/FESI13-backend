package com.fesi.deadlinemate.domain.like.controller;

import com.fesi.deadlinemate.domain.like.service.GatheringLikeService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GatheringLikeController {

    private final GatheringLikeService gatheringLikeService;

    @GetMapping("/likes/ids")
    public ApiResponse<List<Long>> getLikedGatheringIds(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(gatheringLikeService.getLikedGatheringIds(userId));
    }

    @PostMapping("/gatherings/{gatheringId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> like(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        gatheringLikeService.like(gatheringId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/gatherings/{gatheringId}/likes")
    public ApiResponse<Void> unlike(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        gatheringLikeService.unlike(gatheringId, userId);
        return ApiResponse.success();
    }
}
