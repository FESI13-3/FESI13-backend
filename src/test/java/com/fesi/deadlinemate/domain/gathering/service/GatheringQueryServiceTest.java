package com.fesi.deadlinemate.domain.gathering.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fesi.deadlinemate.domain.category.entity.Category;
import com.fesi.deadlinemate.domain.category.entity.GatheringCategory;
import com.fesi.deadlinemate.domain.category.repository.CategoryRepository;
import com.fesi.deadlinemate.domain.category.repository.GatheringCategoryRepository;
import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringDetailResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListItemResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringMainResponse;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringImage;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlan;
import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlanDetail;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringDetailRow;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringListRow;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringImageRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.gathering.repository.WeeklyPlanDetailRepository;
import com.fesi.deadlinemate.domain.gathering.repository.WeeklyPlanRepository;
import com.fesi.deadlinemate.domain.like.repository.GatheringLikeRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.domain.user.entity.Provider;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GatheringQueryServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private GatheringTagRepository gatheringTagRepository;
    @Mock
    private GatheringImageRepository gatheringImageRepository;
    @Mock
    private GatheringCategoryRepository gatheringCategoryRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private WeeklyPlanRepository weeklyPlanRepository;
    @Mock
    private WeeklyPlanDetailRepository weeklyPlanDetailRepository;
    @Mock
    private GatheringMemberRepository gatheringMemberRepository;
    @Mock
    private GatheringLikeRepository gatheringLikeRepository;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private GatheringQueryService gatheringQueryService;

    @Nested
    @DisplayName("모임 조회")
    class GetGatherings {

        @Test
        @DisplayName("page와 limit가 1보다 작으면 1로 보정하여 목록을 조회한다")
        void getGatherings_withInvalidPageAndLimit_success() {
            // given
            GatheringSearchCondition condition = GatheringSearchCondition.builder()
                    .type(GatheringType.STUDY)
                    .categoryIds(List.of(1L, 2L))
                    .sort("latest")
                    .status("recruiting")
                    .query("React")
                    .build();

            GatheringListRow row = gatheringListRow(1L, 10L, "React 스터디");

            given(gatheringRepository.search(eq(condition), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(row), pageable(0, 1), 1));
            given(gatheringTagRepository.findByGatheringIdInOrderByGatheringIdAscIdAsc(List.of(1L)))
                    .willReturn(List.of(
                            GatheringTag.builder().gatheringId(1L).tag("React").build(),
                            GatheringTag.builder().gatheringId(1L).tag("프론트엔드").build()
                    ));
            given(gatheringCategoryRepository.findByGatheringIdIn(List.of(1L)))
                    .willReturn(List.of(
                            GatheringCategory.builder().gatheringId(1L).categoryId(100L).build(),
                            GatheringCategory.builder().gatheringId(1L).categoryId(101L).build()
                    ));
            given(categoryRepository.findByIdIn(List.of(100L, 101L)))
                    .willReturn(List.of(
                            Category.builder().name("개발").build(),
                            Category.builder().name("웹").build()
                    ));
            setId(categoryRepository.findByIdIn(List.of(100L, 101L)).get(0), 100L);
            setId(categoryRepository.findByIdIn(List.of(100L, 101L)).get(1), 101L);

            given(userClient.findByIds(List.of(10L)))
                    .willReturn(List.of(userInfo(10L, "리더")).stream()
                            .collect(java.util.stream.Collectors.toMap(UserInfo::getId, u -> u)));

            // when
            GatheringListResponse result = gatheringQueryService.getGatherings(condition, 0, 0);

            // then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(gatheringRepository).search(eq(condition), pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(0);
            assertThat(pageable.getPageSize()).isEqualTo(1);

            assertThat(result.currentPage()).isEqualTo(1);
            assertThat(result.totalCount()).isEqualTo(1);
            assertThat(result.gatherings()).hasSize(1);

            GatheringListItemResponse item = result.gatherings().get(0);
            assertThat(item.id()).isEqualTo(1L);
            assertThat(item.title()).isEqualTo("React 스터디");
            assertThat(item.type()).isEqualTo("스터디");
            assertThat(item.tags()).containsExactly("React", "프론트엔드");
            assertThat(item.categories()).containsExactly("개발", "웹");
            assertThat(item.leader().id()).isEqualTo(10L);
            assertThat(item.leader().nickname()).isEqualTo("리더");
        }

        @Test
        @DisplayName("조회 결과가 비어 있으면 빈 목록을 반환한다")
        void getGatherings_emptyResult_success() {
            // given
            GatheringSearchCondition condition = GatheringSearchCondition.builder().build();
            Page<GatheringListRow> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 12),
                    0
            );

            given(gatheringRepository.search(eq(condition), any(Pageable.class)))
                    .willReturn(emptyPage);
            // when
            GatheringListResponse result = gatheringQueryService.getGatherings(condition, 1, 12);

            // then
            assertThat(result.gatherings()).isEmpty();
            assertThat(result.totalCount()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.currentPage()).isEqualTo(1);
        }
    }


    @Test
    @DisplayName("popular, deadline, latest 섹션을 각각 조회해 반환한다")
    void getMainGatherings_success() {
        // given
        GatheringListRow popularRow = gatheringListRow(1L, 10L, "인기 모임");
        GatheringListRow deadlineRow = gatheringListRow(2L, 20L, "마감 임박");
        GatheringListRow latestRow = gatheringListRow(3L, 30L, "최신 모임");

        given(gatheringRepository.findMainPopular(3)).willReturn(List.of(popularRow));
        given(gatheringRepository.findMainDeadline(3)).willReturn(List.of(deadlineRow));
        given(gatheringRepository.findMainLatest(3)).willReturn(List.of(latestRow));

        given(gatheringTagRepository.findByGatheringIdInOrderByGatheringIdAscIdAsc(List.of(1L)))
                .willReturn(List.of(GatheringTag.builder().gatheringId(1L).tag("인기").build()));
        given(gatheringTagRepository.findByGatheringIdInOrderByGatheringIdAscIdAsc(List.of(2L)))
                .willReturn(List.of(GatheringTag.builder().gatheringId(2L).tag("마감").build()));
        given(gatheringTagRepository.findByGatheringIdInOrderByGatheringIdAscIdAsc(List.of(3L)))
                .willReturn(List.of(GatheringTag.builder().gatheringId(3L).tag("최신").build()));

        given(gatheringCategoryRepository.findByGatheringIdIn(List.of(1L)))
                .willReturn(List.of(GatheringCategory.builder().gatheringId(1L).categoryId(100L).build()));
        given(gatheringCategoryRepository.findByGatheringIdIn(List.of(2L)))
                .willReturn(List.of(GatheringCategory.builder().gatheringId(2L).categoryId(101L).build()));
        given(gatheringCategoryRepository.findByGatheringIdIn(List.of(3L)))
                .willReturn(List.of(GatheringCategory.builder().gatheringId(3L).categoryId(102L).build()));

        Category c1 = Category.builder().name("개발").build();
        Category c2 = Category.builder().name("디자인").build();
        Category c3 = Category.builder().name("기획").build();
        setId(c1, 100L);
        setId(c2, 101L);
        setId(c3, 102L);

        given(categoryRepository.findByIdIn(List.of(100L))).willReturn(List.of(c1));
        given(categoryRepository.findByIdIn(List.of(101L))).willReturn(List.of(c2));
        given(categoryRepository.findByIdIn(List.of(102L))).willReturn(List.of(c3));

        given(userClient.findByIds(List.of(10L)))
                .willReturn(java.util.Map.of(10L, userInfo(10L, "리더1")));
        given(userClient.findByIds(List.of(20L)))
                .willReturn(java.util.Map.of(20L, userInfo(20L, "리더2")));
        given(userClient.findByIds(List.of(30L)))
                .willReturn(java.util.Map.of(30L, userInfo(30L, "리더3")));

        // when
        GatheringMainResponse result = gatheringQueryService.getMainGatherings(3);

        // then
        assertThat(result.popular()).hasSize(1);
        assertThat(result.deadline()).hasSize(1);
        assertThat(result.latest()).hasSize(1);
        assertThat(result.popular().get(0).title()).isEqualTo("인기 모임");
        assertThat(result.deadline().get(0).title()).isEqualTo("마감 임박");
        assertThat(result.latest().get(0).title()).isEqualTo("최신 모임");

    }

    @Test
    @DisplayName("찜한 모임 ID로 모임 목록을 조회한다")
    void getMyLikedGatherings_success() {
        // given
        Long userId = 99L;
        given(gatheringLikeRepository.findGatheringIdsByUserId(userId))
                .willReturn(List.of(1L, 2L));

        GatheringListRow row1 = gatheringListRow(1L, 10L, "찜한 모임1");
        GatheringListRow row2 = gatheringListRow(2L, 20L, "찜한 모임2");

        given(gatheringRepository.findByIdIn(eq(List.of(1L, 2L)), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(row1, row2), pageable(0, 20), 2));

        given(gatheringTagRepository.findByGatheringIdInOrderByGatheringIdAscIdAsc(List.of(1L, 2L)))
                .willReturn(List.of(
                        GatheringTag.builder().gatheringId(1L).tag("tag1").build(),
                        GatheringTag.builder().gatheringId(2L).tag("tag2").build()
                ));

        given(gatheringCategoryRepository.findByGatheringIdIn(List.of(1L, 2L)))
                .willReturn(List.of());

        given(categoryRepository.findByIdIn(List.of())).willReturn(List.of());

        given(userClient.findByIds(List.of(10L, 20L)))
                .willReturn(java.util.Map.of(
                        10L, userInfo(10L, "리더1"),
                        20L, userInfo(20L, "리더2")
                ));

        // when
        GatheringListResponse result = gatheringQueryService.getMyLikedGatherings(userId, 1, 20);

        // then
        assertThat(result.gatherings()).hasSize(2);
        assertThat(result.gatherings())
                .extracting(GatheringListItemResponse::title)
                .containsExactly("찜한 모임1", "찜한 모임2");

    }

    @Nested
    @DisplayName("getGatheringDetail")
    class GetGatheringDetail {

        @Test
        @DisplayName("모임 상세 정보를 조합해서 반환한다")
        void getGatheringDetail_success() {
            // given
            Long gatheringId = 1L;

            given(gatheringRepository.findDetailRowById(gatheringId))
                    .willReturn(Optional.of(GatheringDetailRow.builder()
                            .id(gatheringId)
                            .leaderId(10L)
                            .type(GatheringType.STUDY)
                            .title("React 완전 정복")
                            .shortDescription("리액트 같이 공부해요")
                            .description("상세 설명입니다.")
                            .goal("완독")
                            .maxMembers(6)
                            .currentMembers(3)
                            .recruitDeadline(LocalDate.of(2026, 4, 20))
                            .startDate(LocalDate.of(2026, 4, 21))
                            .endDate(LocalDate.of(2026, 5, 20))
                            .totalWeeks(4)
                            .status(GatheringStatus.RECRUITING)
                            .build()));

            given(gatheringCategoryRepository.findByGatheringId(gatheringId))
                    .willReturn(List.of(
                            GatheringCategory.builder().gatheringId(gatheringId).categoryId(100L).build(),
                            GatheringCategory.builder().gatheringId(gatheringId).categoryId(101L).build()
                    ));

            Category category1 = Category.builder().name("개발").build();
            Category category2 = Category.builder().name("웹").build();
            setId(category1, 100L);
            setId(category2, 101L);

            given(categoryRepository.findByIdIn(List.of(100L, 101L)))
                    .willReturn(List.of(category1, category2));

            given(gatheringTagRepository.findByGatheringIdOrderByIdAsc(gatheringId))
                    .willReturn(List.of(
                            GatheringTag.builder().gatheringId(gatheringId).tag("React").build(),
                            GatheringTag.builder().gatheringId(gatheringId).tag("스터디").build()
                    ));

            given(gatheringImageRepository.findByGatheringIdOrderByDisplayOrderAsc(gatheringId))
                    .willReturn(List.of(
                            GatheringImage.builder()
                                    .gatheringId(gatheringId)
                                    .imageUrl("https://image.test/1.png")
                                    .displayOrder(0)
                                    .build()
                    ));

            WeeklyPlan weeklyPlan1 = WeeklyPlan.builder()
                    .gatheringId(gatheringId)
                    .weekNumber(1)
                    .title("1주차")
                    .startDate(LocalDate.of(2026, 4, 21))
                    .endDate(LocalDate.of(2026, 4, 27))
                    .build();
            setId(weeklyPlan1, 1000L);

            given(weeklyPlanRepository.findByGatheringIdOrderByWeekNumberAsc(gatheringId))
                    .willReturn(List.of(weeklyPlan1));

            given(weeklyPlanDetailRepository.findByWeeklyPlanIdInOrderByWeeklyPlanIdAscDisplayOrderAsc(List.of(1000L)))
                    .willReturn(List.of(
                            WeeklyPlanDetail.builder()
                                    .weeklyPlanId(1000L)
                                    .displayOrder(0)
                                    .content("공식문서 읽기")
                                    .build(),
                            WeeklyPlanDetail.builder()
                                    .weeklyPlanId(1000L)
                                    .displayOrder(1)
                                    .content("예제 실습")
                                    .build()
                    ));

            GatheringMember leaderMember = GatheringMember.builder()
                    .gatheringId(gatheringId)
                    .userId(10L)
                    .role(GatheringRole.LEADER)
                    .overallAchievementRate(BigDecimal.ZERO)
                    .isActive(true)
                    .build();

            GatheringMember normalMember = GatheringMember.builder()
                    .gatheringId(gatheringId)
                    .userId(20L)
                    .role(GatheringRole.MEMBER)
                    .overallAchievementRate(BigDecimal.ZERO)
                    .isActive(true)
                    .build();

            given(gatheringMemberRepository.findByGatheringIdAndIsActiveTrueOrderByIdAsc(gatheringId))
                    .willReturn(List.of(leaderMember, normalMember));

            given(userClient.findByIds(List.of(10L, 20L)))
                    .willReturn(java.util.Map.of(
                            10L, userInfo(10L, "모임장"),
                            20L, userInfo(20L, "멤버")
                    ));

            given(userClient.findById(10L)).willReturn(userInfo(10L, "모임장"));

            // when
            GatheringDetailResponse result = gatheringQueryService.getGatheringDetail(gatheringId);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.type()).isEqualTo("스터디");
            assertThat(result.categories()).containsExactly("개발", "웹");
            assertThat(result.tags()).containsExactly("React", "스터디");
            assertThat(result.images()).hasSize(1);
            assertThat(result.weeklyPlans()).hasSize(1);
            assertThat(result.weeklyPlans().get(0).details()).containsExactly("공식문서 읽기", "예제 실습");
            assertThat(result.members()).hasSize(2);
            assertThat(result.leader().id()).isEqualTo(10L);
            assertThat(result.leader().nickname()).isEqualTo("모임장");
        }

        @Test
        @DisplayName("존재하지 않는 모임이면 예외가 발생한다")
        void getGatheringDetail_notFound_throwException() {
            // given
            Long gatheringId = 999L;
            given(gatheringRepository.findDetailRowById(gatheringId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> gatheringQueryService.getGatheringDetail(gatheringId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.GATHERING_NOT_FOUND);
        }
    }

    private GatheringListRow gatheringListRow(Long id, Long leaderId, String title) {
        return GatheringListRow.builder()
                .id(id)
                .leaderId(leaderId)
                .type(GatheringType.STUDY)
                .title(title)
                .shortDescription("한 줄 소개")
                .maxMembers(6)
                .currentMembers(3)
                .recruitDeadline(LocalDate.of(2026, 4, 20))
                .startDate(LocalDate.of(2026, 4, 21))
                .endDate(LocalDate.of(2026, 5, 20))
                .status(GatheringStatus.RECRUITING)
                .build();
    }

    private UserInfo userInfo(Long id, String nickname) {
        return UserInfo.builder()
                .id(id)
                .email("test@test.com")
                .nickname(nickname)
                .profileImage("https://profile.test/" + id)
                .provider(Provider.EMAIL)
                .reputationScore(BigDecimal.valueOf(36.5))
                .active(true)
                .build();
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    private void setId(Object target, Long value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}