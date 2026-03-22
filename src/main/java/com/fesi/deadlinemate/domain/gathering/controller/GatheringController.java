package com.fesi.deadlinemate.domain.gathering.controller;

import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.service.GatheringService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
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
}
