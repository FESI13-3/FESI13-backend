package com.fesi.deadlinemate.domain.gatheringApplication.controller;

import com.fesi.deadlinemate.domain.gathering.dto.response.MyApplicationStatusResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.command.CreateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.request.CreateApplicationRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.request.UpdateApplicationRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.ApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.CreateApplicationResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.MyApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.UpdateApplicationResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.service.GatheringApplicationService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GatheringApplicationController {
    private final GatheringApplicationService gatheringApplicationService;

    @PostMapping("/gatherings/{gatheringId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateApplicationResponse> apply(
            @PathVariable Long gatheringId,
            @RequestBody @Valid CreateApplicationRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        CreateApplicationCommand command = request.toCommand(gatheringId, userId);
        CreateApplicationResponse response = gatheringApplicationService.create(command);

        return ApiResponse.success(response);
    }

    @GetMapping("/gatherings/{gatheringId}/applications")
    public ApiResponse<ApplicationListResponse> getApplications(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(gatheringApplicationService.getApplications(gatheringId, userId));
    }

    @PatchMapping("/gatherings/{gatheringId}/applications/{applicationId}")
    public ApiResponse<UpdateApplicationResponse> updateApplication(
            @PathVariable Long gatheringId,
            @PathVariable Long applicationId,
            @RequestBody @Valid UpdateApplicationRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        UpdateApplicationResponse response =
                gatheringApplicationService.updateApplication(
                        request.toCommand(gatheringId, applicationId, userId)
                );

        return ApiResponse.success(response);
    }

    @DeleteMapping("/gatherings/{gatheringId}/applications/{applicationId}")
    public ApiResponse<Void> cancelApplication(
            @PathVariable Long gatheringId,
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        gatheringApplicationService.cancelApplication(gatheringId, applicationId, userId);
        return ApiResponse.success();
    }

    @GetMapping("/gatherings/{gatheringId}/application-status")
    public ApiResponse<MyApplicationStatusResponse> getMyApplicationStatus(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(
                gatheringApplicationService.getMyApplicationStatus(gatheringId, userId)
        );
    }

    @GetMapping("/users/me/applications")
    public ApiResponse<MyApplicationListResponse> getMyApplications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(gatheringApplicationService.getMyApplications(userId));
    }
}
