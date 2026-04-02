package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringDetailResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListItemResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringMainResponse;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringDetailRow;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringListRow;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringImageRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.gathering.repository.WeeklyPlanRepository;
import com.fesi.deadlinemate.domain.like.repository.GatheringLikeRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringQueryService {

    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final GatheringImageRepository gatheringImageRepository;
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringLikeRepository gatheringLikeRepository;
    private final UserClient userClient;

    public GatheringListResponse getGatherings(GatheringSearchCondition condition, int page, int limit) {
        int validatedPage = Math.max(page, 1);
        int validatedLimit = Math.max(limit, 1);

        Pageable pageable = PageRequest.of(validatedPage - 1, validatedLimit);
        Page<GatheringListRow> result = gatheringRepository.search(condition, pageable);

        List<GatheringListItemResponse> items = toListItemResponses(result.getContent());

        return GatheringListResponse.of(
                items,
                result.getTotalElements(),
                result.getTotalPages(),
                validatedPage
        );
    }

    public GatheringMainResponse getMainGatherings(int limit) {
        int validatedLimit = Math.max(limit, 1);

        List<GatheringListItemResponse> popular =
                toListItemResponses(gatheringRepository.findMainPopular(validatedLimit));

        List<GatheringListItemResponse> deadline =
                toListItemResponses(gatheringRepository.findMainDeadline(validatedLimit));

        List<GatheringListItemResponse> latest =
                toListItemResponses(gatheringRepository.findMainLatest(validatedLimit));

        return GatheringMainResponse.of(popular, deadline, latest);
    }

    public GatheringListResponse getMyLikedGatherings(Long userId, int page, int limit) {
        int validatedPage = Math.max(page, 1);
        int validatedLimit = Math.max(limit, 1);

        List<Long> likedGatheringIds = gatheringLikeRepository.findGatheringIdsByUserId(userId);

        Pageable pageable = PageRequest.of(validatedPage - 1, validatedLimit);
        Page<GatheringListRow> result = gatheringRepository.findByIdIn(likedGatheringIds, pageable);

        List<GatheringListItemResponse> items = toListItemResponses(result.getContent());

        return GatheringListResponse.of(
                items,
                result.getTotalElements(),
                result.getTotalPages(),
                validatedPage
        );
    }

    public GatheringDetailResponse getGatheringDetail(Long gatheringId) {
        GatheringDetailRow row = gatheringRepository.findDetailRowById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));

        List<String> tags = gatheringTagRepository.findByGatheringIdOrderByIdAsc(gatheringId).stream()
                .map(GatheringTag::getTag)
                .toList();

        List<GatheringDetailResponse.ImageResponse> images = gatheringImageRepository
                .findByGatheringIdOrderByDisplayOrderAsc(gatheringId).stream()
                .map(image -> GatheringDetailResponse.ImageResponse.builder()
                        .url(image.getImageUrl())
                        .displayOrder(image.getDisplayOrder())
                        .build())
                .toList();

        List<GatheringDetailResponse.WeeklyPlanResponse> weeklyPlans = weeklyPlanRepository
                .findByGatheringIdOrderByWeekNumberAsc(gatheringId).stream()
                .map(plan -> GatheringDetailResponse.WeeklyPlanResponse.builder()
                        .week(plan.getWeekNumber())
                        .title(plan.getTitle())
                        .startDate(plan.getStartDate())
                        .endDate(plan.getEndDate())
                        .build())
                .toList();

        List<GatheringMember> members = gatheringMemberRepository
                .findByGatheringIdAndIsActiveTrueOrderByIdAsc(gatheringId);

        Map<Long, UserInfo> memberUserMap = loadUsers(
                members.stream().map(GatheringMember::getUserId).distinct().toList()
        );

        List<GatheringDetailResponse.MemberResponse> memberResponses = members.stream()
                .map(member -> {
                    UserInfo userInfo = memberUserMap.get(member.getUserId());
                    return GatheringDetailResponse.MemberResponse.builder()
                            .userId(member.getUserId())
                            .nickname(userInfo != null ? userInfo.getNickname() : null)
                            .profileImage(userInfo != null ? userInfo.getProfileImage() : null)
                            .role(member.getRole())
                            .build();
                })
                .toList();

        UserInfo leader = userClient.findById(row.leaderId());

        return GatheringDetailResponse.builder()
                .id(row.id())
                .type(row.type().getDisplayName())
                .category(row.category())
                .title(row.title())
                .shortDescription(row.shortDescription())
                .description(row.description())
                .tags(tags)
                .goal(row.goal())
                .maxMembers(row.maxMembers())
                .currentMembers(row.currentMembers())
                .recruitDeadline(row.recruitDeadline())
                .startDate(row.startDate())
                .endDate(row.endDate())
                .totalWeeks(row.totalWeeks())
                .images(images)
                .status(row.status())
                .leader(GatheringDetailResponse.LeaderResponse.builder()
                        .id(leader.getId())
                        .nickname(leader.getNickname())
                        .profileImage(leader.getProfileImage())
                        .build())
                .weeklyPlans(weeklyPlans)
                .members(memberResponses)
                .build();
    }

    private List<GatheringListItemResponse> toListItemResponses(List<GatheringListRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        List<Long> gatheringIds = rows.stream()
                .map(GatheringListRow::id)
                .toList();

        Map<Long, List<String>> tagsMap = gatheringTagRepository
                .findByGatheringIdInOrderByGatheringIdAscIdAsc(gatheringIds)
                .stream()
                .collect(Collectors.groupingBy(
                        GatheringTag::getGatheringId,
                        LinkedHashMap::new,
                        Collectors.mapping(GatheringTag::getTag, Collectors.toList())
                ));

        Map<Long, UserInfo> leaderMap = loadUsers(
                rows.stream().map(GatheringListRow::leaderId).distinct().toList()
        );

        return rows.stream()
                .map(row -> {
                    UserInfo leader = leaderMap.get(row.leaderId());

                    return GatheringListItemResponse.builder()
                            .id(row.id())
                            .type(row.type().getDisplayName())
                            .category(row.category())
                            .title(row.title())
                            .shortDescription(row.shortDescription())
                            .tags(tagsMap.getOrDefault(row.id(), List.of()))
                            .maxMembers(row.maxMembers())
                            .currentMembers(row.currentMembers())
                            .recruitDeadline(row.recruitDeadline())
                            .startDate(row.startDate())
                            .endDate(row.endDate())
                            .status(row.status())
                            .leader(GatheringListItemResponse.LeaderSummary.builder()
                                    .id(leader != null ? leader.getId() : null)
                                    .nickname(leader != null ? leader.getNickname() : null)
                                    .profileImage(leader != null ? leader.getProfileImage() : null)
                                    .build())
                            .build();
                })
                .toList();
    }

    private Map<Long, UserInfo> loadUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        return userClient.findByIds(userIds);
    }
}
