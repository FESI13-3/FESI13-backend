package com.fesi.deadlinemate.domain.report.controller;

import com.fesi.deadlinemate.domain.report.service.MockReportService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock")
public class MockReportController {
    private final MockReportService service;

    public MockReportController(MockReportService service) {
        this.service = service;
    }

    @GetMapping("/gatherings/{gatheringId}/report")
    public ResponseEntity<ApiResponse<?>> getReport(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getReport(gatheringId)));
    }
}
