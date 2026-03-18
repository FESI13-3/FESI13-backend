package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringDetailResponse;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringServiceImpl implements GatheringService{

    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final UserClient userClient;

    @Override
    @Transactional
    public CreateGatheringResponse createGathering(Long userId, CreateGatheringRequest request) {
        validateUser(userId);
        validateRequest(request);

        int totalWeeks = calculateTotalWeeks(request.getStartDate(), request.getEndDate());
        // TODO : 주차별 계획 생성되면 추가할것
        // validateWeeklyGuides(request.getWeeklyGuides(), totalWeeks);

        Gathering gathering = Gathering.builder()
                .leaderId(userId)
                .type(request.getType())
                .category(request.getCategory())
                .title(request.getTitle())
                .shortDescription(request.getShortDescription())
                .description(request.getDescription())
                .goal(request.getGoal())
                .maxMembers(request.getMaxMembers())
                .recruitDeadline(request.getRecruitDeadline())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalWeeks(totalWeeks)
                .build();

        Gathering savedGathering = gatheringRepository.save(gathering);

        saveTags(savedGathering.getId(), request.getTags());
        //TODO : 주차별 계획, 모임멤버십 추가되면 추가할것
        //saveWeeklyPlans(savedGathering.getId(), request.getWeeklyGuides(), request.getStartDate(), totalWeeks);
        //saveLeaderMembership(savedGathering.getId(), userId, request.getGoal());

        return CreateGatheringResponse.builder()
                .gathering(toResponse(savedGathering, request.getTags()))
                .build();
    }

    private void validateUser(Long userId) {
        if (!userClient.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateRequest(CreateGatheringRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_GATHERING_DATE);
        }

        if (request.getRecruitDeadline().isAfter(request.getStartDate().minusDays(1))) {
            throw new BusinessException(ErrorCode.INVALID_RECRUIT_DEADLINE);
        }

        if (request.getMaxMembers() < 2 || request.getMaxMembers() > 10) {
            throw new BusinessException(ErrorCode.INVALID_MAX_MEMBERS);
        }
    }

    private int calculateTotalWeeks(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return (int) Math.ceil(days / 7.0);
    }

    private void saveTags(Long gatheringId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        List<GatheringTag> gatheringTags = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .map(tag -> new GatheringTag(gatheringId, tag))
                .toList();

        gatheringTagRepository.saveAll(gatheringTags);
    }

    private GatheringDetailResponse toResponse(Gathering gathering, List<String> tags) {
        return GatheringDetailResponse.builder()
                .id(gathering.getId())
                .type(gathering.getType().name())
                .category(gathering.getCategory())
                .title(gathering.getTitle())
                .shortDescription(gathering.getShortDescription())
                .description(gathering.getDescription())
                .goal(gathering.getGoal())
                .tags(tags == null ? List.of() : tags)
                .maxMembers(gathering.getMaxMembers())
                .currentMembers(gathering.getCurrentMembers())
                .recruitDeadline(gathering.getRecruitDeadline())
                .startDate(gathering.getStartDate())
                .endDate(gathering.getEndDate())
                .totalWeeks(gathering.getTotalWeeks())
                .status(gathering.getStatus().name())
                .build();
    }
}
