package com.fesi.deadlinemate.domain.gathering.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlan;
import com.fesi.deadlinemate.domain.gathering.event.GatheringCreatedEvent;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.gathering.repository.WeeklyPlanRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class GatheringServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private GatheringTagRepository gatheringTagRepository;

    @Mock
    private WeeklyPlanRepository weeklyPlanRepository;

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GatheringService gatheringService;

    private CreateGatheringCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = CreateGatheringCommand.builder()
                .leaderId(1L)
                .type(GatheringType.STUDY)
                .category("개발")
                .title("React 완전 정복 스터디")
                .shortDescription("리액트 공식문서를 같이 읽어요")
                .description("매주 공식문서 1챕터씩 읽고 블로그를 작성합니다...")
                .tags(List.of("React", "프론트엔드"))
                .goal("React 공식문서 완독 + 블로그 5편 작성")
                .maxMembers(6)
                .recruitDeadline(LocalDate.of(2025, 3, 20))
                .startDate(LocalDate.of(2025, 3, 22))
                .endDate(LocalDate.of(2025, 4, 19))
                .weeklyGuides(List.of(
                        new CreateGatheringCommand.CreateWeeklyGuideCommand(
                                1, "JSX, 컴포넌트, Props", "공식문서 1~3챕터 읽기"
                        ),
                        new CreateGatheringCommand.CreateWeeklyGuideCommand(
                                2, "State, 이벤트 처리", "공식문서 4~6챕터 읽기"
                        )
                ))
                .imageUrls(List.of())
                .build();
    }

    @Test
    @DisplayName("모임을 생성할 수 있다")
    void createGathering() {
        // given
        given(userClient.existsById(1L)).willReturn(true);
        given(gatheringRepository.save(any(Gathering.class)))
                .willAnswer(invocation -> {
                    Gathering gathering = invocation.getArgument(0);
                    setField(gathering, "id", 100L);
                    return gathering;
                });
        given(gatheringMemberRepository.save(any(GatheringMember.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreateGatheringResponse response = gatheringService.create(validCommand);

        // then
        assertThat(response.id()).isEqualTo(100L);

        ArgumentCaptor<Gathering> gatheringCaptor = ArgumentCaptor.forClass(Gathering.class);
        then(gatheringRepository).should().save(gatheringCaptor.capture());

        Gathering savedGathering = gatheringCaptor.getValue();
        assertThat(savedGathering.getLeaderId()).isEqualTo(1L);
        assertThat(savedGathering.getType()).isEqualTo(GatheringType.STUDY);
        assertThat(savedGathering.getCategory()).isEqualTo("개발");
        assertThat(savedGathering.getTitle()).isEqualTo("React 완전 정복 스터디");
        assertThat(savedGathering.getShortDescription()).isEqualTo("리액트 공식문서를 같이 읽어요");
        assertThat(savedGathering.getDescription()).isEqualTo("매주 공식문서 1챕터씩 읽고 블로그를 작성합니다...");
        assertThat(savedGathering.getGoal()).isEqualTo("React 공식문서 완독 + 블로그 5편 작성");
        assertThat(savedGathering.getMaxMembers()).isEqualTo(6);
        assertThat(savedGathering.getCurrentMembers()).isEqualTo(1);
        assertThat(savedGathering.getRecruitDeadline()).isEqualTo(LocalDate.of(2025, 3, 20));
        assertThat(savedGathering.getStartDate()).isEqualTo(LocalDate.of(2025, 3, 22));
        assertThat(savedGathering.getEndDate()).isEqualTo(LocalDate.of(2025, 4, 19));
        assertThat(savedGathering.getTotalWeeks()).isEqualTo(5);
        assertThat(savedGathering.getStatus()).isEqualTo(GatheringStatus.RECRUITING);

        ArgumentCaptor<List<GatheringTag>> tagCaptor = ArgumentCaptor.forClass(List.class);
        then(gatheringTagRepository).should().saveAll(tagCaptor.capture());

        assertThat(tagCaptor.getValue())
                .extracting(GatheringTag::getGatheringId, GatheringTag::getTag)
                .containsExactly(
                        tuple(100L, "React"),
                        tuple(100L, "프론트엔드")
                );

        ArgumentCaptor<List<WeeklyPlan>> weeklyPlanCaptor = ArgumentCaptor.forClass(List.class);
        then(weeklyPlanRepository).should().saveAll(weeklyPlanCaptor.capture());

        assertThat(weeklyPlanCaptor.getValue())
                .extracting(
                        WeeklyPlan::getGatheringId,
                        WeeklyPlan::getWeekNumber,
                        WeeklyPlan::getTitle,
                        WeeklyPlan::getContent,
                        WeeklyPlan::getStartDate,
                        WeeklyPlan::getEndDate
                )
                .containsExactly(
                        tuple(
                                100L,
                                1,
                                "JSX, 컴포넌트, Props",
                                "공식문서 1~3챕터 읽기",
                                LocalDate.of(2025, 3, 22),
                                LocalDate.of(2025, 3, 28)
                        ),
                        tuple(
                                100L,
                                2,
                                "State, 이벤트 처리",
                                "공식문서 4~6챕터 읽기",
                                LocalDate.of(2025, 3, 29),
                                LocalDate.of(2025, 4, 4)
                        )
                );

        ArgumentCaptor<GatheringMember> memberCaptor = ArgumentCaptor.forClass(GatheringMember.class);
        then(gatheringMemberRepository).should().save(memberCaptor.capture());

        GatheringMember leaderMember = memberCaptor.getValue();
        assertThat(leaderMember.getGatheringId()).isEqualTo(100L);
        assertThat(leaderMember.getUserId()).isEqualTo(1L);
        assertThat(leaderMember.getRole()).isEqualTo(GatheringRole.LEADER);
        assertThat(leaderMember.isActive()).isTrue();

        ArgumentCaptor<GatheringCreatedEvent> eventCaptor = ArgumentCaptor.forClass(GatheringCreatedEvent.class);
        then(eventPublisher).should().publishEvent(eventCaptor.capture());

        GatheringCreatedEvent event = eventCaptor.getValue();
        assertThat(event.gatheringId()).isEqualTo(100L);
        assertThat(event.leaderId()).isEqualTo(1L);
        assertThat(event.title()).isEqualTo("React 완전 정복 스터디");
    }

    @Nested
    @DisplayName("모임 생성 실패")
    class Fail {

        @Test
        @DisplayName("존재하지 않는 유저는 모임을 생성할 수 없다")
        void failWhenLeaderDoesNotExist() {
            given(userClient.existsById(1L)).willReturn(false);

            assertThatThrownBy(() -> gatheringService.create(validCommand))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);

            then(gatheringRepository).should(never()).save(any());
            then(gatheringTagRepository).should(never()).saveAll(any());
            then(weeklyPlanRepository).should(never()).saveAll(any());
            then(gatheringMemberRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishEvent(any());
        }

        @Test
        @DisplayName("최대 인원이 2명 미만이면 예외가 발생한다")
        void failWhenMaxMembersTooSmall() {
            CreateGatheringCommand command = validCommand.toBuilder()
                    .maxMembers(1)
                    .build();

            given(userClient.existsById(1L)).willReturn(true);

            assertThatThrownBy(() -> gatheringService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_MAX_MEMBERS);

            then(gatheringRepository).should(never()).save(any());
            then(gatheringTagRepository).should(never()).saveAll(any());
            then(weeklyPlanRepository).should(never()).saveAll(any());
            then(gatheringMemberRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishEvent(any());
        }

        @Test
        @DisplayName("모집 마감일이 시작일보다 늦으면 예외가 발생한다")
        void failWhenRecruitDeadlineAfterStartDate() {
            CreateGatheringCommand command = validCommand.toBuilder()
                    .recruitDeadline(LocalDate.of(2025, 3, 23))
                    .build();

            given(userClient.existsById(1L)).willReturn(true);

            assertThatThrownBy(() -> gatheringService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_RECRUIT_DEADLINE);

            then(gatheringRepository).should(never()).save(any());
            then(gatheringTagRepository).should(never()).saveAll(any());
            then(weeklyPlanRepository).should(never()).saveAll(any());
            then(gatheringMemberRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishEvent(any());
        }

        @Test
        @DisplayName("종료일이 시작일보다 빠르면 예외가 발생한다")
        void failWhenEndDateBeforeStartDate() {
            CreateGatheringCommand command = validCommand.toBuilder()
                    .endDate(LocalDate.of(2025, 3, 21))
                    .build();

            given(userClient.existsById(1L)).willReturn(true);

            assertThatThrownBy(() -> gatheringService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_GATHERING_DATE);

            then(gatheringRepository).should(never()).save(any());
            then(gatheringTagRepository).should(never()).saveAll(any());
            then(weeklyPlanRepository).should(never()).saveAll(any());
            then(gatheringMemberRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishEvent(any());
        }

        @Test
        @DisplayName("주차 가이드가 1주차부터 순차적이지 않으면 예외가 발생한다")
        void failWhenWeeklyGuideIsNotSequential() {
            CreateGatheringCommand command = validCommand.toBuilder()
                    .weeklyGuides(List.of(
                            new CreateGatheringCommand.CreateWeeklyGuideCommand(1, "1주차", "내용"),
                            new CreateGatheringCommand.CreateWeeklyGuideCommand(3, "3주차", "내용")
                    ))
                    .build();

            given(userClient.existsById(1L)).willReturn(true);

            assertThatThrownBy(() -> gatheringService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.BAD_REQUEST);

            then(gatheringRepository).should(never()).save(any());
            then(gatheringTagRepository).should(never()).saveAll(any());
            then(weeklyPlanRepository).should(never()).saveAll(any());
            then(gatheringMemberRepository).should(never()).save(any());
            then(eventPublisher).should(never()).publishEvent(any());
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}