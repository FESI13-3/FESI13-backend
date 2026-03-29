package com.fesi.deadlinemate.domain.report.controller;

import com.fesi.deadlinemate.domain.report.dto.GatheringReportResponse;
import com.fesi.deadlinemate.domain.report.service.GatheringReportQueryService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringReportController {

    private final GatheringReportQueryService gatheringReportQueryService;

    @GetMapping("/{gatheringId}/report")
    public ApiResponse<GatheringReportResponse> getReport(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(
                gatheringReportQueryService.getReport(gatheringId, userId)
        );
    }
}
