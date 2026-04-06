package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.dto.response.MemberListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipQueryService {

    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final ReviewRepository reviewRepository;
    private final UserClient userClient;

    public MyGatheringListResponse getMyGatherings(Long userId, String status, String sort, int page, int limit) {
        int validatedPage = Math.max(page, 1);
        int validatedLimit = Math.max(limit, 1);

        List<Long> gatheringIds = gatheringMemberRepository.findActiveGatheringIdsByUserId(userId);

        if (gatheringIds.isEmpty()) {
            return MyGatheringListResponse.builder()
                    .gatherings(List.of())
                    .totalCount(0)
                    .totalPages(0)
                    .currentPage(validatedPage)
                    .build();
        }

        boolean isOldest = "oldest".equalsIgnoreCase(sort);
        PageRequest pageable = PageRequest.of(validatedPage - 1, validatedLimit);
        Page<Gathering> result = resolveGatheringPage(gatheringIds, status, isOldest, pageable);

        List<Long> resultGatheringIds = result.getContent().stream()
                .map(Gathering::getId).toList();

        Map<Long, GatheringMember> memberMap = gatheringMemberRepository
                .findByGatheringIdInAndUserIdAndIsActiveTrue(resultGatheringIds, userId).stream()
                .collect(Collectors.toMap(GatheringMember::getGatheringId, m -> m));

        Map<Long, List<String>> tagsMap = gatheringTagRepository
                .findByGatheringIdInOrderByGatheringIdAscIdAsc(resultGatheringIds).stream()
                .collect(Collectors.groupingBy(
                        GatheringTag::getGatheringId,
                        LinkedHashMap::new,
                        Collectors.mapping(GatheringTag::getTag, Collectors.toList())
                ));

        Set<Long> reviewedGatheringIds = Set.copyOf(
                reviewRepository.findReviewedGatheringIds(userId, resultGatheringIds)
        );

        List<MyGatheringListResponse.MyGatheringItem> items = result.getContent().stream()
                .map(gathering -> {
                    GatheringMember member = memberMap.get(gathering.getId());
                    List<String> tags = tagsMap.getOrDefault(gathering.getId(), List.of());
                    return MyGatheringListResponse.MyGatheringItem.of(
                            gathering,
                            member != null ? member.getRole() : null,
                            tags,
                            reviewedGatheringIds.contains(gathering.getId())
                    );
                })
                .toList();

        return MyGatheringListResponse.builder()
                .gatherings(items)
                .totalCount(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .currentPage(validatedPage)
                .build();
    }

    public MemberListResponse getMembers(Long gatheringId, Long requesterId) {
        validateMembership(gatheringId, requesterId);

        List<GatheringMember> members = gatheringMemberRepository
                .findByGatheringIdAndIsActiveTrueOrderByIdAsc(gatheringId);

        List<Long> userIds = members.stream().map(GatheringMember::getUserId).distinct().toList();
        Map<Long, UserInfo> userMap = userClient.findByIds(userIds);

        return MemberListResponse.of(members, userMap);
    }

    private Page<Gathering> resolveGatheringPage(List<Long> ids, String status, boolean isOldest, PageRequest pageable) {
        GatheringStatus gatheringStatus = GatheringStatus.fromString(status);
        if (gatheringStatus != null) {
            return isOldest
                    ? gatheringRepository.findByIdInAndStatusOrderByCreatedAtAsc(ids, gatheringStatus, pageable)
                    : gatheringRepository.findByIdInAndStatusOrderByCreatedAtDesc(ids, gatheringStatus, pageable);
        }
        return isOldest
                ? gatheringRepository.findByIdInOrderByCreatedAtAsc(ids, pageable)
                : gatheringRepository.findByIdInOrderByCreatedAtDesc(ids, pageable);
    }

    private void validateMembership(Long gatheringId, Long userId) {
        if (!gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(gatheringId, userId)) {
            throw new BusinessException(ErrorCode.NOT_A_MEMBER);
        }
    }
}
