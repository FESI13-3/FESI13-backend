package com.fesi.deadlinemate.domain.gatheringApplication.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gatheringApplication.command.CreateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.command.UpdateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.ApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.CreateApplicationResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.MyApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.UpdateApplicationResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationCancelledEvent;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationCreatedEvent;
import com.fesi.deadlinemate.domain.gatheringApplication.event.GatheringApplicationUpdatedEvent;
import com.fesi.deadlinemate.domain.gatheringApplication.repository.GatheringApplicationRepository;
import com.fesi.deadlinemate.domain.review.client.ReviewClient;
import com.fesi.deadlinemate.domain.review.client.dto.ApplicantReviewInfo;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
class GatheringApplicationServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private GatheringApplicationRepository gatheringApplicationRepository;

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ReviewClient reviewClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GatheringApplicationService gatheringApplicationService;

    private CreateApplicationCommand command;
    private Gathering recruitingGathering;

    @BeforeEach
    void setUp() {
        command = CreateApplicationCommand.builder()
                .gatheringId(100L)
                .applicantId(200L)
                .personalGoal("  스프링 부트 실력 향상  ")
                .selfIntroduction("  백엔드 3개월차입니다.  ")
                .build();

        recruitingGathering = Gathering.builder()
                .leaderId(10L)
                .type(GatheringType.STUDY)
                .title("Spring Study")
                .shortDescription("스프링 같이 공부해요")
                .description("매주 학습하고 공유합니다.")
                .goal("스프링 완주")
                .maxMembers(5)
                .currentMembers(3)
                .recruitDeadline(LocalDate.now().plusDays(3))
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(26))
                .totalWeeks(4)
                .status(GatheringStatus.RECRUITING)
                .viewCount(0)
                .build();

        setField(recruitingGathering, "id", 100L);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("정상 신청이면 저장 후 응답을 반환하고 이벤트를 발행한다")
        void create_success() {
            // given
            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.existsByGatheringIdAndApplicantId(100L, 200L)).thenReturn(false);
            when(gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(100L, 200L)).thenReturn(false);

            GatheringApplication savedApplication = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("스프링 부트 실력 향상")
                    .selfIntroduction("백엔드 3개월차입니다.")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(savedApplication, "id", 999L);

            when(gatheringApplicationRepository.saveAndFlush(any(GatheringApplication.class)))
                    .thenReturn(savedApplication);

            // when
            CreateApplicationResponse response = gatheringApplicationService.create(command);

            // then
            assertThat(response.id()).isEqualTo(999L);
            assertThat(response.status()).isEqualTo(ApplicationStatus.PENDING);

            ArgumentCaptor<GatheringApplication> applicationCaptor =
                    ArgumentCaptor.forClass(GatheringApplication.class);

            verify(gatheringApplicationRepository).saveAndFlush(applicationCaptor.capture());

            GatheringApplication captured = applicationCaptor.getValue();
            assertThat(captured.getGatheringId()).isEqualTo(100L);
            assertThat(captured.getApplicantId()).isEqualTo(200L);
            assertThat(captured.getPersonalGoal()).isEqualTo("스프링 부트 실력 향상");
            assertThat(captured.getSelfIntroduction()).isEqualTo("백엔드 3개월차입니다.");
            assertThat(captured.getStatus()).isEqualTo(ApplicationStatus.PENDING);

            ArgumentCaptor<GatheringApplicationCreatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(GatheringApplicationCreatedEvent.class);

            verify(eventPublisher).publishEvent(eventCaptor.capture());

            GatheringApplicationCreatedEvent event = eventCaptor.getValue();
            assertThat(event.applicationId()).isEqualTo(999L);
            assertThat(event.gatheringId()).isEqualTo(100L);
            assertThat(event.applicantId()).isEqualTo(200L);
            assertThat(event.leaderId()).isEqualTo(10L);
            assertThat(event.gatheringTitle()).isEqualTo("Spring Study");
        }

        @Test
        @DisplayName("selfIntroduction이 blank면 null로 저장한다")
        void create_blankSelfIntroduction_savedAsNull() {
            // given
            CreateApplicationCommand blankIntroCommand = CreateApplicationCommand.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("  목표  ")
                    .selfIntroduction("   ")
                    .build();

            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.existsByGatheringIdAndApplicantId(100L, 200L)).thenReturn(false);
            when(gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(100L, 200L)).thenReturn(false);

            GatheringApplication savedApplication = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("목표")
                    .selfIntroduction(null)
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(savedApplication, "id", 1L);

            when(gatheringApplicationRepository.saveAndFlush(any(GatheringApplication.class)))
                    .thenReturn(savedApplication);

            // when
            gatheringApplicationService.create(blankIntroCommand);

            // then
            ArgumentCaptor<GatheringApplication> applicationCaptor =
                    ArgumentCaptor.forClass(GatheringApplication.class);

            verify(gatheringApplicationRepository).saveAndFlush(applicationCaptor.capture());
            assertThat(applicationCaptor.getValue().getSelfIntroduction()).isNull();
        }

        @Test
        @DisplayName("모임장은 자기 모임에 신청할 수 없다")
        void create_fail_leaderCannotApply() {
            // given
            setField(recruitingGathering, "leaderId", 200L);

            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));

            // when & then
            assertThatThrownBy(() -> gatheringApplicationService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.GATHERING_LEADER_CANNOT_APPLY);

            verify(gatheringApplicationRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("모집 중 상태가 아니면 신청할 수 없다")
        void create_fail_notRecruiting() {
            // given
            setField(recruitingGathering, "status", GatheringStatus.IN_PROGRESS);

            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));

            // when & then
            assertThatThrownBy(() -> gatheringApplicationService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.GATHERING_NOT_RECRUITING);

            verify(gatheringApplicationRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("정원이 가득 찬 모임은 신청할 수 없다")
        void create_fail_full() {
            // given
            setField(recruitingGathering, "currentMembers", 5);

            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));

            // when & then
            assertThatThrownBy(() -> gatheringApplicationService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.GATHERING_FULL);

            verify(gatheringApplicationRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("이미 신청한 유저는 중복 신청할 수 없다")
        void create_fail_duplicateApplication() {
            // given
            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.existsByGatheringIdAndApplicantId(100L, 200L)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> gatheringApplicationService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_GATHERING_APPLICATION);

            verify(gatheringApplicationRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("이미 활성 멤버인 유저는 신청할 수 없다")
        void create_fail_alreadyMember() {
            // given
            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.existsByGatheringIdAndApplicantId(100L, 200L)).thenReturn(false);
            when(gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(100L, 200L)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> gatheringApplicationService.create(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.ALREADY_GATHERING_MEMBER);

            verify(gatheringApplicationRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("getApplications")
    class GetApplications {

        @Test
        @DisplayName("모임장이면 신청 목록을 조회할 수 있다")
        void getApplications_success() {
            GatheringApplication application1 = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("목표1")
                    .selfIntroduction("소개1")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application1, "id", 1L);
            setField(application1, "createdAt", LocalDateTime.of(2025, 3, 15, 10, 0));

            GatheringApplication application2 = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(201L)
                    .personalGoal("목표2")
                    .selfIntroduction(null)
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application2, "id", 2L);
            setField(application2, "createdAt", LocalDateTime.of(2025, 3, 15, 11, 0));

            UserInfo userInfo1 = UserInfo.builder()
                    .id(200L)
                    .nickname("유저1")
                    .profileImage("profile1.png")
                    .reputationScore(BigDecimal.valueOf(36.5))
                    .build();

            UserInfo userInfo2 = UserInfo.builder()
                    .id(201L)
                    .nickname("유저2")
                    .profileImage("profile2.png")
                    .reputationScore(BigDecimal.valueOf(40.0))
                    .build();

            ApplicantReviewInfo reviewInfo1 = ApplicantReviewInfo.builder()
                    .reviewCount(2)
                    .topTags(List.of("성실해요", "소통이 좋아요"))
                    .recentReviews(List.of(
                            ApplicantReviewInfo.RecentReview.builder()
                                    .id(11L)
                                    .comment("좋은 팀원이었어요")
                                    .tags(List.of("성실해요"))
                                    .build()
                    ))
                    .build();

            ApplicantReviewInfo reviewInfo2 = ApplicantReviewInfo.builder()
                    .reviewCount(0)
                    .topTags(List.of())
                    .recentReviews(List.of())
                    .build();

            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.findByGatheringIdOrderByCreatedAtAsc(100L))
                    .thenReturn(List.of(application1, application2));
            when(userClient.findByIds(List.of(200L, 201L))).thenReturn(Map.of(
                    200L, userInfo1,
                    201L, userInfo2
            ));
            when(reviewClient.getApplicantReviewInfos(List.of(200L, 201L))).thenReturn(Map.of(
                    200L, reviewInfo1,
                    201L, reviewInfo2
            ));

            ApplicationListResponse response = gatheringApplicationService.getApplications(100L, 10L);

            assertThat(response.applications()).hasSize(2);

            ApplicationListResponse.ApplicationItemResponse first = response.applications().get(0);
            assertThat(first.id()).isEqualTo(1L);
            assertThat(first.applicant().nickname()).isEqualTo("유저1");
            assertThat(first.applicant().reviewSummary().reviewCount()).isEqualTo(2);
            assertThat(first.applicant().reviewSummary().topTags())
                    .containsExactly("성실해요", "소통이 좋아요");
            assertThat(first.applicant().recentReviews()).hasSize(1);
            assertThat(first.applicant().recentReviews().get(0).comment()).isEqualTo("좋은 팀원이었어요");

            ApplicationListResponse.ApplicationItemResponse second = response.applications().get(1);
            assertThat(second.id()).isEqualTo(2L);
            assertThat(second.applicant().nickname()).isEqualTo("유저2");
            assertThat(second.applicant().reviewSummary().reviewCount()).isEqualTo(0);
            assertThat(second.applicant().recentReviews()).isEmpty();

            verify(userClient, times(1)).findByIds(List.of(200L, 201L));
            verify(reviewClient, times(1)).getApplicantReviewInfos(List.of(200L, 201L));
            verify(userClient, never()).findById(anyLong());
        }

        @Test
        @DisplayName("모임장이 아니면 조회할 수 없다")
        void getApplications_fail_notLeader() {
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));

            assertThatThrownBy(() -> gatheringApplicationService.getApplications(100L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_GATHERING_LEADER);

            verify(gatheringApplicationRepository, never()).findByGatheringIdOrderByCreatedAtAsc(any());
        }
    }

    @Nested
    @DisplayName("updateApplication")
    class UpdateApplication {

        @Test
        @DisplayName("수락하면 신청 상태가 ACCEPTED로 변경되고 멤버가 추가되며 이벤트를 발행한다")
        void updateApplication_accept_success() {
            UpdateApplicationCommand updateCommand = UpdateApplicationCommand.builder()
                    .gatheringId(100L)
                    .applicationId(1L)
                    .requesterId(10L)
                    .status(ApplicationStatus.ACCEPTED)
                    .build();

            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("개인 목표")
                    .selfIntroduction("소개")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application, "id", 1L);

            when(gatheringRepository.findByIdForUpdate(100L))
                    .thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.findByIdAndGatheringIdForUpdate(1L, 100L))
                    .thenReturn(Optional.of(application));
            when(gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(100L, 200L))
                    .thenReturn(false);

            UpdateApplicationResponse response = gatheringApplicationService.updateApplication(updateCommand);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.status()).isEqualTo(ApplicationStatus.ACCEPTED);
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
            assertThat(recruitingGathering.getCurrentMembers()).isEqualTo(4);

            ArgumentCaptor<GatheringMember> memberCaptor = ArgumentCaptor.forClass(GatheringMember.class);
            verify(gatheringMemberRepository).saveAndFlush(memberCaptor.capture());

            GatheringMember savedMember = memberCaptor.getValue();
            assertThat(savedMember.getGatheringId()).isEqualTo(100L);
            assertThat(savedMember.getUserId()).isEqualTo(200L);
            assertThat(savedMember.getRole()).isEqualTo(GatheringRole.MEMBER);
            assertThat(savedMember.getPersonalGoal()).isEqualTo("개인 목표");

            ArgumentCaptor<GatheringApplicationUpdatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(GatheringApplicationUpdatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            GatheringApplicationUpdatedEvent event = eventCaptor.getValue();
            assertThat(event.applicationId()).isEqualTo(1L);
            assertThat(event.status()).isEqualTo(ApplicationStatus.ACCEPTED);
        }

        @Test
        @DisplayName("거절하면 신청 상태가 REJECTED로 변경되고 이벤트를 발행한다")
        void updateApplication_reject_success() {
            UpdateApplicationCommand updateCommand = UpdateApplicationCommand.builder()
                    .gatheringId(100L)
                    .applicationId(1L)
                    .requesterId(10L)
                    .status(ApplicationStatus.REJECTED)
                    .build();

            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("개인 목표")
                    .selfIntroduction("소개")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application, "id", 1L);

            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.findByIdAndGatheringId(1L, 100L)).thenReturn(Optional.of(application));

            UpdateApplicationResponse response = gatheringApplicationService.updateApplication(updateCommand);

            assertThat(response.status()).isEqualTo(ApplicationStatus.REJECTED);
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REJECTED);

            verify(gatheringMemberRepository, never()).saveAndFlush(any());

            ArgumentCaptor<GatheringApplicationUpdatedEvent> eventCaptor =
                    ArgumentCaptor.forClass(GatheringApplicationUpdatedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().status()).isEqualTo(ApplicationStatus.REJECTED);
        }

        @Test
        @DisplayName("변경 가능한 상태가 아니면 예외가 발생한다")
        void updateApplication_fail_invalidStatus() {
            UpdateApplicationCommand updateCommand = UpdateApplicationCommand.builder()
                    .gatheringId(100L)
                    .applicationId(1L)
                    .requesterId(10L)
                    .status(ApplicationStatus.PENDING)
                    .build();

            assertThatThrownBy(() -> gatheringApplicationService.updateApplication(updateCommand))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_APPLICATION_STATUS_CHANGE);

            verify(gatheringRepository, never()).findById(any());
            verify(gatheringRepository, never()).findByIdForUpdate(any());
            verify(gatheringApplicationRepository, never()).findByIdAndGatheringId(any(), any());
            verify(gatheringApplicationRepository, never()).findByIdAndGatheringIdForUpdate(any(), any());
        }

        @Test
        @DisplayName("정원이 가득 찬 경우 수락할 수 없다")
        void updateApplication_fail_full() {
            setField(recruitingGathering, "currentMembers", 5);

            UpdateApplicationCommand updateCommand = UpdateApplicationCommand.builder()
                    .gatheringId(100L)
                    .applicationId(1L)
                    .requesterId(10L)
                    .status(ApplicationStatus.ACCEPTED)
                    .build();

            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("개인 목표")
                    .selfIntroduction("소개")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application, "id", 1L);

            when(gatheringRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(recruitingGathering));
            when(gatheringApplicationRepository.findByIdAndGatheringIdForUpdate(1L, 100L))
                    .thenReturn(Optional.of(application));

            assertThatThrownBy(() -> gatheringApplicationService.updateApplication(updateCommand))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.GATHERING_FULL);

            verify(gatheringMemberRepository, never()).saveAndFlush(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("cancelApplication")
    class CancelApplication {

        @Test
        @DisplayName("본인의 PENDING 신청이면 취소할 수 있고 이벤트를 발행한다")
        void cancelApplication_success() {
            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("개인 목표")
                    .selfIntroduction("소개")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application, "id", 1L);

            when(gatheringApplicationRepository.findByIdAndGatheringId(1L, 100L))
                    .thenReturn(Optional.of(application));
            when(gatheringRepository.findById(100L)).thenReturn(Optional.of(recruitingGathering));

            gatheringApplicationService.cancelApplication(100L, 1L, 200L);

            verify(gatheringApplicationRepository).delete(application);

            ArgumentCaptor<GatheringApplicationCancelledEvent> eventCaptor =
                    ArgumentCaptor.forClass(GatheringApplicationCancelledEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            GatheringApplicationCancelledEvent event = eventCaptor.getValue();
            assertThat(event.applicationId()).isEqualTo(1L);
            assertThat(event.gatheringId()).isEqualTo(100L);
            assertThat(event.applicantId()).isEqualTo(200L);
            assertThat(event.leaderId()).isEqualTo(10L);
            assertThat(event.gatheringTitle()).isEqualTo("Spring Study");
        }

        @Test
        @DisplayName("본인 신청이 아니면 취소할 수 없다")
        void cancelApplication_fail_forbidden() {
            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(201L)
                    .personalGoal("개인 목표")
                    .selfIntroduction("소개")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application, "id", 1L);

            when(gatheringApplicationRepository.findByIdAndGatheringId(1L, 100L))
                    .thenReturn(Optional.of(application));

            assertThatThrownBy(() -> gatheringApplicationService.cancelApplication(100L, 1L, 200L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.APPLICATION_CANCEL_FORBIDDEN);

            verify(gatheringApplicationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("PENDING 상태가 아니면 취소할 수 없다")
        void cancelApplication_fail_notPending() {
            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("개인 목표")
                    .selfIntroduction("소개")
                    .status(ApplicationStatus.ACCEPTED)
                    .build();
            setField(application, "id", 1L);

            when(gatheringApplicationRepository.findByIdAndGatheringId(1L, 100L))
                    .thenReturn(Optional.of(application));

            assertThatThrownBy(() -> gatheringApplicationService.cancelApplication(100L, 1L, 200L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_APPLICATION_STATUS_CHANGE);

            verify(gatheringApplicationRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("getMyApplications")
    class GetMyApplications {

        @Test
        @DisplayName("내 신청 목록을 모임 정보와 함께 조회한다")
        void getMyApplications_success() {
            GatheringApplication application1 = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("목표1")
                    .selfIntroduction("소개1")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application1, "id", 1L);
            setField(application1, "createdAt", LocalDateTime.of(2025, 3, 15, 10, 0));

            GatheringApplication application2 = GatheringApplication.builder()
                    .gatheringId(101L)
                    .applicantId(200L)
                    .personalGoal("목표2")
                    .selfIntroduction("소개2")
                    .status(ApplicationStatus.REJECTED)
                    .build();
            setField(application2, "id", 2L);
            setField(application2, "createdAt", LocalDateTime.of(2025, 3, 16, 10, 0));

            Gathering gathering2 = Gathering.builder()
                    .leaderId(11L)
                    .type(GatheringType.PROJECT)
                    .title("Project Gathering")
                    .shortDescription("프로젝트")
                    .description("설명")
                    .goal("목표")
                    .maxMembers(5)
                    .currentMembers(2)
                    .recruitDeadline(LocalDate.now().plusDays(1))
                    .startDate(LocalDate.now().plusDays(3))
                    .endDate(LocalDate.now().plusDays(20))
                    .totalWeeks(3)
                    .status(GatheringStatus.RECRUITING)
                    .viewCount(0)
                    .build();
            setField(gathering2, "id", 101L);

            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(200L))
                    .thenReturn(List.of(application2, application1));
            when(gatheringRepository.findAllById(List.of(101L, 100L)))
                    .thenReturn(List.of(gathering2, recruitingGathering));

            MyApplicationListResponse response = gatheringApplicationService.getMyApplications(200L);

            assertThat(response.applications()).hasSize(2);
            assertThat(response.applications().get(0).id()).isEqualTo(2L);
            assertThat(response.applications().get(0).gathering().title()).isEqualTo("Project Gathering");
            assertThat(response.applications().get(1).id()).isEqualTo(1L);
            assertThat(response.applications().get(1).gathering().title()).isEqualTo("Spring Study");
        }

        @Test
        @DisplayName("신청에 대응되는 모임 정보가 없으면 예외가 발생한다")
        void getMyApplications_fail_missingGathering() {
            GatheringApplication application = GatheringApplication.builder()
                    .gatheringId(100L)
                    .applicantId(200L)
                    .personalGoal("목표1")
                    .selfIntroduction("소개1")
                    .status(ApplicationStatus.PENDING)
                    .build();
            setField(application, "id", 1L);

            when(userClient.existsById(200L)).thenReturn(true);
            when(gatheringApplicationRepository.findByApplicantIdOrderByCreatedAtDesc(200L))
                    .thenReturn(List.of(application));
            when(gatheringRepository.findAllById(List.of(100L)))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> gatheringApplicationService.getMyApplications(200L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(ex -> ((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.GATHERING_NOT_FOUND);
        }
    }


    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}