package com.fesi.deadlinemate.domain.gathering.controller;

import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.request.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.UpdateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.service.GatheringService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {
    private final GatheringService gatheringService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateGatheringResponse> create(
            @RequestPart("request") @Valid CreateGatheringRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        CreateGatheringCommand command = request.toCommand(userId);
        CreateGatheringResponse response = gatheringService.create(command);

        return ApiResponse.success(response);
    }

    @PutMapping(value = "/{gatheringId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UpdateGatheringResponse> update(
            @PathVariable Long gatheringId,
            @RequestBody @Valid UpdateGatheringRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        UpdateGatheringResponse response = gatheringService.update(gatheringId, request.toCommand(userId));
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{gatheringId}")
    public ApiResponse<Void> delete(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        gatheringService.delete(gatheringId, userId);
        return ApiResponse.success();
    }
}
