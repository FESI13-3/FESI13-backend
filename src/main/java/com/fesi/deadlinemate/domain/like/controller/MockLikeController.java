package com.fesi.deadlinemate.domain.like.controller;

import com.fesi.deadlinemate.domain.like.service.MockLikeService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/mock")
public class MockLikeController {
    private final MockLikeService service;

    public MockLikeController(MockLikeService service) {
        this.service = service;
    }

    @GetMapping("/users/me/likes/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getLikedGatheringIds() {
        return ResponseEntity.ok(ApiResponse.success(service.getLikedGatheringIds()));
    }

    @GetMapping("/users/me/likes")
    public ResponseEntity<ApiResponse<?>> getMyLikedGatherings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getMyLikedGatherings(page, limit)));
    }

    @PostMapping("/gatherings/{gatheringId}/likes")
    public ResponseEntity<ApiResponse<?>> like(@PathVariable Long gatheringId) {
        service.like(gatheringId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(java.util.Map.of("success", true), "찜하기가 완료되었습니다."));
    }

    @DeleteMapping("/gatherings/{gatheringId}/likes")
    public ResponseEntity<ApiResponse<?>> unlike(@PathVariable Long gatheringId) {
        service.unlike(gatheringId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "찜하기가 취소되었습니다."));
    }
}
