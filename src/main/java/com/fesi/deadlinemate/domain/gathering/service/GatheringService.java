package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlan;
import com.fesi.deadlinemate.domain.gathering.event.GatheringCreatedEvent;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.gathering.repository.WeeklyPlanRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final UserClient userClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateGatheringResponse create(CreateGatheringCommand command) {
        validateLeader(command.leaderId());
        validateCommand(command);

        Gathering gathering = Gathering.builder()
                .leaderId(command.leaderId())
                .type(command.type())
                .category(command.category())
                .title(command.title())
                .shortDescription(command.shortDescription())
                .description(command.description())
                .goal(command.goal())
                .maxMembers(command.maxMembers())
                .currentMembers(1)
                .recruitDeadline(command.recruitDeadline())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .totalWeeks(calculateTotalWeeks(command.startDate(), command.endDate()))
                .status(GatheringStatus.RECRUITING)
                .viewCount(0)
                .build();

        Gathering saved = gatheringRepository.save(gathering);

        saveTags(saved.getId(), command.tags());
        saveWeeklyPlans(saved.getId(), command.weeklyGuides(), command.startDate(), command.endDate());
        saveLeaderMember(saved.getId(), command.leaderId());

        eventPublisher.publishEvent(new GatheringCreatedEvent(
                saved.getId(),
                saved.getLeaderId(),
                saved.getTitle()
        ));

        return CreateGatheringResponse.from(saved, command.tags());
    }

    private void validateLeader(Long leaderId) {
        if (leaderId == null || !userClient.existsById(leaderId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateCommand(CreateGatheringCommand command) {
        if (command.maxMembers() < 2 || command.maxMembers() > 10) {
            throw new BusinessException(ErrorCode.INVALID_MAX_MEMBERS);
        }

        if (command.recruitDeadline().isAfter(command.startDate())) {
            throw new BusinessException(ErrorCode.INVALID_RECRUIT_DEADLINE);
        }

        if (command.endDate().isBefore(command.startDate())) {
            throw new BusinessException(ErrorCode.INVALID_GATHERING_DATE);
        }

        validateWeeklyGuides(command.weeklyGuides());
    }

    private void validateWeeklyGuides(List<CreateGatheringCommand.CreateWeeklyGuideCommand> weeklyGuides) {
        if (weeklyGuides == null || weeklyGuides.isEmpty()) {
            return;
        }

        for (int i = 0; i < weeklyGuides.size(); i++) {
            int expectedWeek = i + 1;
            if (weeklyGuides.get(i).week() != expectedWeek) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "주차 가이드는 1주차부터 순차적으로 입력되어야 합니다.");
            }
        }
    }

    private int calculateTotalWeeks(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return (int) (days / 7) + 1;
    }

    private void saveTags(Long gatheringId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        List<GatheringTag> entities = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .map(tag -> GatheringTag.builder()
                        .gatheringId(gatheringId)
                        .tag(tag)
                        .build())
                .toList();

        if (!entities.isEmpty()) {
            gatheringTagRepository.saveAll(entities);
        }
    }

    private void saveWeeklyPlans(
            Long gatheringId,
            List<CreateGatheringCommand.CreateWeeklyGuideCommand> weeklyGuides,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (weeklyGuides == null || weeklyGuides.isEmpty()) {
            return;
        }

        List<WeeklyPlan> entities = weeklyGuides.stream()
                .map(guide -> {
                    LocalDate weekStart = startDate.plusWeeks(guide.week() - 1L);
                    LocalDate weekEnd = weekStart.plusDays(6);
                    if (weekEnd.isAfter(endDate)) {
                        weekEnd = endDate;
                    }

                    return WeeklyPlan.builder()
                            .gatheringId(gatheringId)
                            .weekNumber(guide.week())
                            .title(guide.title())
                            .content(guide.content())
                            .startDate(weekStart)
                            .endDate(weekEnd)
                            .build();
                })
                .toList();

        weeklyPlanRepository.saveAll(entities);
    }

    private void saveLeaderMember(Long gatheringId, Long leaderId) {
        GatheringMember leaderMember = GatheringMember.builder()
                .gatheringId(gatheringId)
                .userId(leaderId)
                .role(GatheringRole.LEADER)
                .personalGoal(null)
                .overallAchievementRate(BigDecimal.ZERO.setScale(2))
                .isActive(true)
                .build();

        gatheringMemberRepository.save(leaderMember);
    }
}
