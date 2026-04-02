package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.category.entity.Category;
import com.fesi.deadlinemate.domain.category.entity.GatheringCategory;
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
import com.fesi.deadlinemate.domain.gathering.event.GatheringCompletedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringCreatedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringDeletedEvent;
import com.fesi.deadlinemate.domain.gathering.event.GatheringUpdatedEvent;
import com.fesi.deadlinemate.domain.category.repository.CategoryRepository;
import com.fesi.deadlinemate.domain.category.repository.GatheringCategoryRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.gathering.repository.WeeklyPlanRepository;
import com.fesi.deadlinemate.domain.like.repository.GatheringLikeRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringTagRepository gatheringTagRepository;
    private final GatheringCategoryRepository gatheringCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final WeeklyPlanRepository weeklyPlanRepository;
    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringLikeRepository gatheringLikeRepository;
    private final UserClient userClient;
    private final ApplicationEventPublisher eventPublisher;

    public CreateGatheringResponse create(CreateGatheringCommand command) {
        validateLeaderExists(command.leaderId());
        validateSequentialWeeks(extractCreateGuideWeeks(command.weeklyGuides()));

        List<Category> categories = validateAndLoadCategories(command.categoryIds());

        Gathering gathering = Gathering.builder()
                .leaderId(command.leaderId())
                .type(command.type())
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

        saveCategories(saved.getId(), categories);
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

        return CreateGatheringResponse.from(saved,
                categories.stream().map(Category::getName).toList(),
                normalizeTags(command.tags()));
    }

    public UpdateGatheringResponse update(Long gatheringId, UpdateGatheringCommand command) {
        Gathering gathering = findGathering(gatheringId);
        gathering.validateLeader(command.requesterId());

        return switch (gathering.getStatus()) {
            case RECRUITING -> updateRecruitingGathering(gathering, command);
            case IN_PROGRESS -> updateInProgressGathering(gathering, command);
            default -> throw new BusinessException(ErrorCode.GATHERING_UPDATE_FORBIDDEN_IN_PROGRESS);
        };
    }

    public void delete(Long gatheringId, Long requesterId) {
        Gathering gathering = findGathering(gatheringId);
        gathering.validateLeader(requesterId);
        gathering.validateDeletable();

        weeklyPlanRepository.deleteByGatheringId(gatheringId);
        gatheringCategoryRepository.deleteByGatheringId(gatheringId);
        gatheringTagRepository.deleteByGatheringId(gatheringId);
        gatheringMemberRepository.deleteByGatheringId(gatheringId);
        gatheringLikeRepository.deleteByGatheringId(gatheringId);
        gatheringRepository.delete(gathering);

        eventPublisher.publishEvent(new GatheringDeletedEvent(
                gathering.getId(),
                gathering.getLeaderId(),
                gathering.getTitle()
        ));
    }

    public void completeEndedGatherings(LocalDate today) {
        List<Gathering> gatherings = gatheringRepository.findByStatusAndEndDateLessThanEqual(
                GatheringStatus.IN_PROGRESS,
                today
        );

        for (Gathering gathering : gatherings) {
            gathering.complete();

            eventPublisher.publishEvent(new GatheringCompletedEvent(
                    gathering.getId(),
                    gathering.getLeaderId(),
                    gathering.getTitle()
            ));
        }
    }

    private UpdateGatheringResponse updateRecruitingGathering(Gathering gathering, UpdateGatheringCommand command) {
        validateSequentialWeeks(extractUpdateGuideWeeks(command.weeklyGuides()));
        List<Category> categories = validateAndLoadCategories(command.categoryIds());

        gathering.updateRecruiting(
                command.type(),
                command.title(),
                command.shortDescription(),
                command.description(),
                command.goal(),
                command.maxMembers(),
                command.recruitDeadline(),
                command.startDate(),
                command.endDate()
        );

        List<String> normalizedTags = normalizeTags(command.tags());
        replaceCategories(gathering.getId(), categories);
        replaceTags(gathering.getId(), normalizedTags);
        replaceWeeklyPlansFromUpdate(
                gathering.getId(),
                command.weeklyGuides(),
                command.startDate(),
                command.endDate()
        );

        publishGatheringUpdatedEvent(gathering);
        return UpdateGatheringResponse.from(
                gathering,
                categories.stream().map(Category::getName).toList(),
                normalizedTags
        );
    }

    private UpdateGatheringResponse updateInProgressGathering(Gathering gathering, UpdateGatheringCommand command) {
        validateSequentialWeeks(extractUpdateGuideWeeks(command.weeklyGuides()));

        List<Long> currentCategoryIds = findCategoryIds(gathering.getId());
        List<Long> requestedCategoryIds = normalizeCategoryIds(command.categoryIds());

        List<String> currentTags = findTags(gathering.getId());
        List<String> requestedTags = normalizeTags(command.tags());

        gathering.updateInProgress(
                command.type(),
                command.title(),
                command.shortDescription(),
                command.description(),
                command.goal(),
                command.maxMembers(),
                command.recruitDeadline(),
                command.startDate(),
                command.endDate(),
                requestedCategoryIds,
                currentCategoryIds,
                requestedTags,
                currentTags
        );


        replaceWeeklyPlansFromUpdate(
                gathering.getId(),
                command.weeklyGuides(),
                gathering.getStartDate(),
                command.endDate()
        );

        publishGatheringUpdatedEvent(gathering);
        List<String> currentCategoryNames = findCategoryNames(currentCategoryIds);
        return UpdateGatheringResponse.from(gathering, currentCategoryNames, currentTags);
    }

    private List<Category> validateAndLoadCategories(List<Long> categoryIds) {
        List<Long> normalizedIds = normalizeCategoryIds(categoryIds);
        if (normalizedIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "카테고리는 최소 1개 이상 필요합니다.");
        }

        List<Category> categories = categoryRepository.findByIdIn(normalizedIds);
        if (categories.size() != normalizedIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 카테고리가 포함되어 있습니다.");
        }
        return categories;
    }

    private List<Long> normalizeCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        List<Long> normalized = categoryIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (normalized.size() > 3) {
            throw new BusinessException(ErrorCode.GATHERING_CATEGORY_COUNT);
        }

        return normalized;
    }

    private void saveCategories(Long gatheringId, List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }

        List<GatheringCategory> entities = categories.stream()
                .map(category -> GatheringCategory.builder()
                        .gatheringId(gatheringId)
                        .categoryId(category.getId())
                        .build())
                .toList();

        gatheringCategoryRepository.saveAll(entities);
    }

    private void replaceCategories(Long gatheringId, List<Category> categories) {
        gatheringCategoryRepository.deleteByGatheringId(gatheringId);
        saveCategories(gatheringId, categories);
    }

    private List<Long> findCategoryIds(Long gatheringId) {
        return gatheringCategoryRepository.findByGatheringId(gatheringId).stream()
                .map(GatheringCategory::getCategoryId)
                .toList();
    }

    private List<String> findCategoryNames(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        Map<Long, String> categoryNameMap = categoryRepository.findByIdIn(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return categoryIds.stream()
                .map(categoryNameMap::get)
                .filter(Objects::nonNull)
                .toList();
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