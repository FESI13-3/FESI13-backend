package com.fesi.deadlinemate.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.review.dto.response.ReviewListResponse;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.entity.ReviewTag;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReviewQueryServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private GatheringClient gatheringClient;
    @Mock private UserClient userClient;
    @InjectMocks private ReviewQueryService reviewQueryService;

    @Test
    @DisplayName("배치 메서드로 리뷰어/모임 정보를 한 번에 조회한다")
    void getReviews() {
        Review review = createReview(1L, 1L, 10L, 20L, null);
        given(reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(userClient.findByIds(List.of(10L))).willReturn(
                Map.of(10L, UserInfo.builder().id(10L).nickname("reviewer").build()));
        given(gatheringClient.findTitlesByIds(List.of(1L))).willReturn(
                Map.of(1L, "Test Gathering"));
        given(reviewRepository.countMatesTagsByTargetUserId(20L)).willReturn(List.of());

        ReviewListResponse response = reviewQueryService.getReviews(20L, 1);

        assertThat(response.reviews()).hasSize(1);
        assertThat(response.totalCount()).isEqualTo(1L);
        assertThat(response.reviews().get(0).reviewerNickname()).isEqualTo("reviewer");
        assertThat(response.reviews().get(0).gatheringTitle()).isEqualTo("Test Gathering");
        assertThat(response.reviews().get(0).tags()).containsExactly("성실해요");
        assertThat(response.matesTagCounts()).isEmpty();
    }

    @Test
    @DisplayName("matesTag가 있는 리뷰는 matesTag와 집계 결과를 반환한다")
    void getReviewsWithMatesTag() {
        Review review = createReview(1L, 1L, 10L, 20L, "최고의 메이트에요");
        given(reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(userClient.findByIds(any())).willReturn(Map.of());
        given(gatheringClient.findTitlesByIds(any())).willReturn(Map.of());
        List<Object[]> mateCounts = new java.util.ArrayList<>();
        mateCounts.add(new Object[]{"최고의 메이트에요", 1L});
        given(reviewRepository.countMatesTagsByTargetUserId(20L)).willReturn(mateCounts);

        ReviewListResponse response = reviewQueryService.getReviews(20L, 1);

        assertThat(response.reviews().get(0).matesTag()).isEqualTo("최고의 메이트에요");
        assertThat(response.matesTagCounts()).hasSize(1);
        assertThat(response.matesTagCounts().get(0).tag()).isEqualTo("최고의 메이트에요");
        assertThat(response.matesTagCounts().get(0).count()).isEqualTo(1L);
    }

    private Review createReview(Long id, Long gatheringId, Long reviewerId, Long targetUserId, String matesTag) {
        Review review = Review.create(gatheringId, reviewerId, targetUserId,
                List.of(ReviewTag.DILIGENT.getDisplayName()), matesTag, "좋았습니다");
        try {
            Field f = Review.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(review, id);
        } catch (Exception e) { throw new RuntimeException(e); }
        return review;
    }
}
