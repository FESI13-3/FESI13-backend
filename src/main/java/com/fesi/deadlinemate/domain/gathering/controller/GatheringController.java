package com.fesi.deadlinemate.domain.gathering.controller;

import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.dto.request.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringDetailResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringMainResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.UpdateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.dto.response.MemberListResponse;
import com.fesi.deadlinemate.domain.gathering.service.GatheringQueryService;
import com.fesi.deadlinemate.domain.gathering.service.GatheringService;
import com.fesi.deadlinemate.domain.gathering.service.MembershipCommandService;
import com.fesi.deadlinemate.domain.gathering.service.MembershipQueryService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import com.fesi.deadlinemate.global.common.ImageStorageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class GatheringController {
    private final GatheringService gatheringService;
    private final GatheringQueryService gatheringQueryService;
    private final MembershipCommandService membershipCommandService;
    private final MembershipQueryService membershipQueryService;
    private final ImageStorageService imageStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateGatheringResponse> create(
            @RequestPart("request") @Valid CreateGatheringRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        List<String> imageUrls = (images != null && !images.isEmpty())
                ? imageStorageService.uploadAll(images, "gatherings")
                : List.of();

        CreateGatheringCommand command = request.toCommand(userId, imageUrls);
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

    @GetMapping
    public ApiResponse<GatheringListResponse> getGatherings(
            @RequestParam(required = false) GatheringType type,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "recruiting") String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        GatheringSearchCondition condition = new GatheringSearchCondition(type, categoryIds, sort, status, query);

        return ApiResponse.success(gatheringQueryService.getGatherings(condition, page, limit));
    }

    @GetMapping("/main")
    public ApiResponse<GatheringMainResponse> getMainGatherings(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.success(gatheringQueryService.getMainGatherings(limit));
    }

    @GetMapping("/{gatheringId}")
    public ApiResponse<GatheringDetailResponse> getGatheringDetail(
            @PathVariable Long gatheringId
    ) {
        return ApiResponse.success(gatheringQueryService.getGatheringDetail(gatheringId));
    }

    @GetMapping("/{gatheringId}/members")
    public ApiResponse<MemberListResponse> getMembers(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(membershipQueryService.getMembers(gatheringId, userId));
    }

    @DeleteMapping("/{gatheringId}/members/{targetUserId}")
    public ApiResponse<Void> kickMember(
            @PathVariable Long gatheringId,
            @PathVariable Long targetUserId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        membershipCommandService.kickMember(gatheringId, targetUserId, userId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{gatheringId}/members/me")
    public ApiResponse<Void> leaveGathering(
            @PathVariable Long gatheringId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        membershipCommandService.leaveGathering(gatheringId, userId);
        return ApiResponse.success();
    }
}
