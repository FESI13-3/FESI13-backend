package com.fesi.deadlinemate.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.review.dto.response.ReviewListResponse;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.entity.ReviewTag;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
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
    @DisplayName("GatheringClient와 UserClient를 통해 리뷰 목록을 조회한다")
    void getReviews() {
        Review review = createReview(1L, 1L, 10L, 20L);
        given(reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(review)));
        given(userClient.findById(10L)).willReturn(
                UserInfo.builder().id(10L).nickname("reviewer").build());
        given(gatheringClient.findById(1L)).willReturn(Optional.of(
                GatheringInfo.builder().id(1L).title("Test").status(GatheringStatus.COMPLETED).build()));

        ReviewListResponse response = reviewQueryService.getReviews(20L, 1);

        assertThat(response.reviews()).hasSize(1);
        assertThat(response.reviews().get(0).reviewerNickname()).isEqualTo("reviewer");
        assertThat(response.reviews().get(0).gatheringTitle()).isEqualTo("Test");
        assertThat(response.reviews().get(0).tags()).containsExactly("성실해요");
    }

    private Review createReview(Long id, Long gatheringId, Long reviewerId, Long targetUserId) {
        Review review = Review.create(gatheringId, reviewerId, targetUserId,
                List.of(ReviewTag.DILIGENT.getDisplayName()), "좋았습니다");
        try {
            Field f = Review.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(review, id);
        } catch (Exception e) { throw new RuntimeException(e); }
        return review;
    }
}
