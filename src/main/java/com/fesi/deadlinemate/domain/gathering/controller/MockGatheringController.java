package com.fesi.deadlinemate.domain.gathering.controller;

import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.MockGatheringDtos.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.service.MockGatheringService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MockGatheringController {

    private final MockGatheringService service;

    public MockGatheringController(MockGatheringService service) {
        this.service = service;
    }

    @GetMapping("/gatherings")
    public ResponseEntity<ApiResponse<?>> getGatherings(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "recruiting") String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getGatherings(type, category, sort, status, query, page, limit)
        ));
    }

    @PostMapping("/gatherings")
    public ResponseEntity<ApiResponse<?>> createGathering(@RequestBody CreateGatheringRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        java.util.Map.of("gathering", service.createGathering(request)),
                        "모임이 생성되었습니다."
                ));
    }

    @GetMapping("/gatherings/main")
    public ResponseEntity<ApiResponse<?>> getMain(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(service.getMain(limit)));
    }

    @GetMapping("/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<?>> getGatheringDetail(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getGatheringDetail(gatheringId)));
    }

    @PutMapping("/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<?>> updateGathering(
            @PathVariable Long gatheringId,
            @RequestBody UpdateGatheringRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of("gathering", service.updateGathering(gatheringId, request)),
                "모임이 수정되었습니다."
        ));
    }

    @DeleteMapping("/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<?>> deleteGathering(@PathVariable Long gatheringId) {
        service.deleteGathering(gatheringId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "모임이 삭제되었습니다."));
    }
}
