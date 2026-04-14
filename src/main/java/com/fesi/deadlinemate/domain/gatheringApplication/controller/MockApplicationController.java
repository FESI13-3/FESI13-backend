package com.fesi.deadlinemate.domain.gatheringApplication.controller;

import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.CreateApplicationRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.MockApplicationDtos.UpdateApplicationStatusRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.service.MockApplicationService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/mock")
public class MockApplicationController {

    private final MockApplicationService service;

    public MockApplicationController(MockApplicationService service) {
        this.service = service;
    }

    @PostMapping("/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<?>> createApplication(
            @PathVariable Long gatheringId,
            @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createApplication(gatheringId, request), "신청이 완료되었습니다."));
    }

    @GetMapping("/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<?>> getApplications(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getApplications(gatheringId)));
    }

    @PatchMapping("/gatherings/{gatheringId}/applications/{applicationId}")
    public ResponseEntity<ApiResponse<?>> updateApplicationStatus(
            @PathVariable Long gatheringId,
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updateApplicationStatus(gatheringId, applicationId, request),
                "신청 상태가 변경되었습니다."
        ));
    }

    @DeleteMapping("/gatherings/{gatheringId}/applications/{applicationId}")
    public ResponseEntity<ApiResponse<?>> cancelApplication(
            @PathVariable Long gatheringId,
            @PathVariable Long applicationId
    ) {
        service.cancelApplication(gatheringId, applicationId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "신청이 취소되었습니다."));
    }

    @GetMapping("/users/me/applications")
    public ResponseEntity<ApiResponse<?>> getMyApplications() {
        return ResponseEntity.ok(ApiResponse.success(service.getMyApplications()));
    }
}
