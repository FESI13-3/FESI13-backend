package com.fesi.deadlinemate.domain.achievement;

import com.fesi.deadlinemate.domain.achievement.service.MockAchievementService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MockAchievementController {
    private final MockAchievementService service;

    public MockAchievementController(MockAchievementService service) {
        this.service = service;
    }

    @GetMapping("/gatherings/{gatheringId}/achievements")
    public ResponseEntity<ApiResponse<?>> getAchievements(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAchievements(gatheringId)));
    }

    @GetMapping("/gatherings/{gatheringId}/achievements/ranking")
    public ResponseEntity<ApiResponse<?>> getRanking(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAchievementRanking(gatheringId)));
    }
}
