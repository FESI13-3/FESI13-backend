package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.command.UpdateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.UpdateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlan;
import com.fesi.deadlinemate.domain.gathering.event.GatheringCreatedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringDeletedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringUpdatedEvent;
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
        validateLeaderExists(command.leaderId());
        validateCreateCommand(command);

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
        saveWeeklyPlansFromCreate(
                saved.getId(),
                command.weeklyGuides(),
                command.startDate(),
                command.endDate()
        );
        saveLeaderMember(saved.getId(), command.leaderId());

        eventPublisher.publishEvent(new GatheringCreatedEvent(
                saved.getId(),
                saved.getLeaderId(),
                saved.getTitle()
        ));

        return CreateGatheringResponse.from(saved, normalizeTags(command.tags()));
    }

    @Transactional
    public UpdateGatheringResponse update(Long gatheringId, UpdateGatheringCommand command) {
        Gathering gathering = findGathering(gatheringId);
        validateLeaderPermission(gathering, command.requesterId());

        if (gathering.getStatus() == GatheringStatus.RECRUITING) {
            return updateRecruitingGathering(gathering, command);
        }

        if (gathering.getStatus() == GatheringStatus.IN_PROGRESS) {
            return updateInProgressGathering(gathering, command);
        }

        throw new BusinessException(ErrorCode.GATHERING_UPDATE_FORBIDDEN_IN_PROGRESS);
    }

    @Transactional
    public void delete(Long gatheringId, Long requesterId) {
        Gathering gathering = findGathering(gatheringId);
        validateLeaderPermission(gathering, requesterId);
        validateDeletableStatus(gathering);

        weeklyPlanRepository.deleteByGatheringId(gatheringId);
        gatheringTagRepository.deleteByGatheringId(gatheringId);
        gatheringMemberRepository.deleteByGatheringId(gatheringId);
        gatheringRepository.delete(gathering);

        eventPublisher.publishEvent(new GatheringDeletedEvent(
                gathering.getId(),
                gathering.getLeaderId(),
                gathering.getTitle()
        ));
    }

    private UpdateGatheringResponse updateRecruitingGathering(Gathering gathering, UpdateGatheringCommand command) {
        validateRecruitingUpdate(command);

        gathering.updateRecruitingInfo(
                command.type(),
                command.category(),
                command.title(),
                command.shortDescription(),
                command.description(),
                command.goal(),
                command.maxMembers(),
                command.recruitDeadline(),
                command.startDate(),
                command.endDate(),
                calculateTotalWeeks(command.startDate(), command.endDate())
        );

        List<String> normalizedTags = normalizeTags(command.tags());
        replaceTags(gathering.getId(), normalizedTags);
        replaceWeeklyPlansFromUpdate(
                gathering.getId(),
                command.weeklyGuides(),
                command.startDate(),
                command.endDate()
        );

        publishGatheringUpdatedEvent(gathering);

        return UpdateGatheringResponse.from(gathering, normalizedTags);
    }

    private UpdateGatheringResponse updateInProgressGathering(Gathering gathering, UpdateGatheringCommand command) {
        List<String> currentTags = findTags(gathering.getId());

        validateInProgressUpdate(command, gathering, currentTags);

        gathering.updateInProgressInfo(
                command.description(),
                command.endDate(),
                calculateTotalWeeks(gathering.getStartDate(), command.endDate())
        );

        replaceWeeklyPlansFromUpdate(
                gathering.getId(),
                command.weeklyGuides(),
                gathering.getStartDate(),
                command.endDate()
        );

        publishGatheringUpdatedEvent(gathering);

        return UpdateGatheringResponse.from(gathering, currentTags);
    }

    private void publishGatheringUpdatedEvent(Gathering gathering) {
        eventPublisher.publishEvent(new GatheringUpdatedEvent(
                gathering.getId(),
                gathering.getLeaderId(),
                gathering.getStatus()
        ));
    }

    private Gathering findGathering(Long gatheringId) {
        return gatheringRepository.findById(gatheringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
    }

    private void validateLeaderExists(Long leaderId) {
        if (leaderId == null || !userClient.existsById(leaderId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateLeaderPermission(Gathering gathering, Long requesterId) {
        if (requesterId == null || !gathering.getLeaderId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.INVALID_GATHERING_LEADER);
        }
    }

    private void validateDeletableStatus(Gathering gathering) {
        if (gathering.getStatus() == GatheringStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.GATHERING_DELETE_NOT_ALLOWED);
        }
    }

    private void validateCreateCommand(CreateGatheringCommand command) {
        validateMaxMembers(command.maxMembers());
        validateRecruitDeadline(command.recruitDeadline(), command.startDate());
        validateDateRange(command.startDate(), command.endDate());
        validateSequentialWeeks(
                extractCreateGuideWeeks(command.weeklyGuides())
        );
    }

    private void validateRecruitingUpdate(UpdateGatheringCommand command) {
        validateMaxMembers(command.maxMembers());
        validateRecruitDeadline(command.recruitDeadline(), command.startDate());
        validateDateRange(command.startDate(), command.endDate());
        validateSequentialWeeks(
                extractUpdateGuideWeeks(command.weeklyGuides())
        );
    }

    private void validateInProgressUpdate(
            UpdateGatheringCommand command,
            Gathering gathering,
            List<String> currentTags
    ) {
        validateDateRange(gathering.getStartDate(), command.endDate());

        List<String> requestedTags = normalizeTags(command.tags());

        boolean typeChanged = command.type() != gathering.getType();
        boolean categoryChanged = !Objects.equals(command.category(), gathering.getCategory());
        boolean titleChanged = !Objects.equals(command.title(), gathering.getTitle());
        boolean shortDescriptionChanged = !Objects.equals(command.shortDescription(), gathering.getShortDescription());
        boolean goalChanged = !Objects.equals(command.goal(), gathering.getGoal());
        boolean maxMembersChanged = !Objects.equals(command.maxMembers(), gathering.getMaxMembers());
        boolean recruitDeadlineChanged = !Objects.equals(command.recruitDeadline(), gathering.getRecruitDeadline());
        boolean startDateChanged = !Objects.equals(command.startDate(), gathering.getStartDate());
        boolean tagsChanged = !Objects.equals(requestedTags, currentTags);

        if (typeChanged || categoryChanged || titleChanged || shortDescriptionChanged
                || goalChanged || maxMembersChanged || recruitDeadlineChanged
                || startDateChanged || tagsChanged) {
            throw new BusinessException(ErrorCode.INVALID_IN_PROGRESS_UPDATE_ITEMS);
        }

        validateSequentialWeeks(
                extractUpdateGuideWeeks(command.weeklyGuides())
        );
    }

    private void validateMaxMembers(int maxMembers) {
        if (maxMembers < 2 || maxMembers > 10) {
            throw new BusinessException(ErrorCode.INVALID_MAX_MEMBERS);
        }
    }

    private void validateRecruitDeadline(LocalDate recruitDeadline, LocalDate startDate) {
        if (recruitDeadline.isAfter(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_RECRUIT_DEADLINE);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_GATHERING_DATE);
        }
    }

    private void validateSequentialWeeks(List<Integer> weeks) {
        if (weeks == null || weeks.isEmpty()) {
            return;
        }

        for (int i = 0; i < weeks.size(); i++) {
            if (weeks.get(i) != i + 1) {
                throw new BusinessException(ErrorCode.INVALID_WEEKLY_GUIDE_SEQUENCE);
            }
        }
    }

    private List<Integer> extractCreateGuideWeeks(List<CreateGatheringCommand.CreateWeeklyGuideCommand> weeklyGuides) {
        if (weeklyGuides == null || weeklyGuides.isEmpty()) {
            return List.of();
        }

        return weeklyGuides.stream()
                .map(CreateGatheringCommand.CreateWeeklyGuideCommand::week)
                .toList();
    }

    private List<Integer> extractUpdateGuideWeeks(List<UpdateGatheringCommand.UpdateWeeklyGuideCommand> weeklyGuides) {
        if (weeklyGuides == null || weeklyGuides.isEmpty()) {
            return List.of();
        }

        return weeklyGuides.stream()
                .map(UpdateGatheringCommand.UpdateWeeklyGuideCommand::week)
                .toList();
    }

    private int calculateTotalWeeks(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        return (int) (days / 7) + 1;
    }

    private void saveTags(Long gatheringId, List<String> tags) {
        List<String> normalizedTags = normalizeTags(tags);
        if (normalizedTags.isEmpty()) {
            return;
        }

        List<GatheringTag> entities = normalizedTags.stream()
                .map(tag -> GatheringTag.builder()
                        .gatheringId(gatheringId)
                        .tag(tag)
                        .build())
                .toList();

        gatheringTagRepository.saveAll(entities);
    }

    private void replaceTags(Long gatheringId, List<String> tags) {
        gatheringTagRepository.deleteByGatheringId(gatheringId);
        saveTags(gatheringId, tags);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }

        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
    }

    private List<String> findTags(Long gatheringId) {
        return gatheringTagRepository.findByGatheringIdOrderByIdAsc(gatheringId).stream()
                .map(GatheringTag::getTag)
                .toList();
    }

    private void saveWeeklyPlansFromCreate(
            Long gatheringId,
            List<CreateGatheringCommand.CreateWeeklyGuideCommand> weeklyGuides,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (weeklyGuides == null || weeklyGuides.isEmpty()) {
            return;
        }

        weeklyPlanRepository.saveAll(
                weeklyGuides.stream()
                        .map(guide -> buildWeeklyPlan(
                                gatheringId,
                                guide.week(),
                                guide.title(),
                                guide.content(),
                                startDate,
                                endDate
                        ))
                        .toList()
        );
    }

    private void replaceWeeklyPlansFromUpdate(
            Long gatheringId,
            List<UpdateGatheringCommand.UpdateWeeklyGuideCommand> weeklyGuides,
            LocalDate startDate,
            LocalDate endDate
    ) {
        weeklyPlanRepository.deleteByGatheringId(gatheringId);

        if (weeklyGuides == null || weeklyGuides.isEmpty()) {
            return;
        }

        weeklyPlanRepository.saveAll(
                weeklyGuides.stream()
                        .map(guide -> buildWeeklyPlan(
                                gatheringId,
                                guide.week(),
                                guide.title(),
                                guide.content(),
                                startDate,
                                endDate
                        ))
                        .toList()
        );
    }

    private WeeklyPlan buildWeeklyPlan(
            Long gatheringId,
            int weekNumber,
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return WeeklyPlan.builder()
                .gatheringId(gatheringId)
                .weekNumber(weekNumber)
                .title(title)
                .content(content)
                .startDate(calculateWeekStartDate(startDate, weekNumber))
                .endDate(calculateWeekEndDate(startDate, endDate, weekNumber))
                .build();
    }

    private LocalDate calculateWeekStartDate(LocalDate startDate, int weekNumber) {
        return startDate.plusWeeks(weekNumber - 1L);
    }

    private LocalDate calculateWeekEndDate(LocalDate startDate, LocalDate endDate, int weekNumber) {
        LocalDate weekStart = calculateWeekStartDate(startDate, weekNumber);
        LocalDate weekEnd = weekStart.plusDays(6);
        return weekEnd.isAfter(endDate) ? endDate : weekEnd;
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