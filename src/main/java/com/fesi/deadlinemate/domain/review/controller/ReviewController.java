package com.fesi.deadlinemate.domain.review.controller;

import com.fesi.deadlinemate.domain.review.dto.request.CreateReviewRequest;
import com.fesi.deadlinemate.domain.review.dto.response.ReviewListResponse;
import com.fesi.deadlinemate.domain.review.service.ReviewCommandService;
import com.fesi.deadlinemate.domain.review.service.ReviewQueryService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping("/api/v1/gatherings/{gatheringId}/reviews")
    public ApiResponse<Void> createReviews(
            @PathVariable Long gatheringId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication
    ) {
        Long reviewerId = (Long) authentication.getPrincipal();
        reviewCommandService.createReviews(request.toCommand(gatheringId, reviewerId));
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/users/{userId}/reviews")
    public ApiResponse<ReviewListResponse> getReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ApiResponse.success(reviewQueryService.getReviews(userId, page));
    }
}
