package com.fesi.deadlinemate.domain.gathering.controller;

import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.service.GatheringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {
    private final GatheringService gatheringService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGatheringResponse createGathering(
            //@AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody CreateGatheringRequest request
    ) {
        //return gatheringService.createGathering(principal.getUserId(), request);
        return null;
    }
}
