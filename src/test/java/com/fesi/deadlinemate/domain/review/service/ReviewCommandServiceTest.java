package com.fesi.deadlinemate.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.gathering.client.dto.GatheringInfo;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.review.command.CreateReviewCommand;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.math.BigDecimal;
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

@ExtendWith(MockitoExtension.class)
class ReviewCommandServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private GatheringClient gatheringClient;
    @Mock private UserClient userClient;
    @InjectMocks private ReviewCommandService reviewCommandService;

    @Nested
    @DisplayName("리뷰 작성")
    class CreateReviews {

        @Test
        @DisplayName("완료된 모임에 리뷰를 작성한다")
        void createReviews() {
            given(gatheringClient.findById(1L)).willReturn(Optional.of(completedGatheringInfo()));
            given(reviewRepository.existsByGatheringIdAndReviewerId(1L, 10L)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willAnswer(inv -> inv.getArgument(0));

            CreateReviewCommand command = new CreateReviewCommand(1L, 10L, List.of(
                    new CreateReviewCommand.ReviewItem(20L, List.of("성실해요", "소통이 좋아요"), "좋았습니다")
            ));

            reviewCommandService.createReviews(command);

            ArgumentCaptor<Review> captor = ArgumentCaptor.forClass(Review.class);
            then(reviewRepository).should().save(captor.capture());
            assertThat(captor.getValue().getTagCount()).isEqualTo(2);
            then(userClient).should().addReputationScore(eq(20L), any(BigDecimal.class));
        }

        @Test
        @DisplayName("완료되지 않은 모임이면 예외가 발생한다")
        void gatheringNotCompleted() {
            given(gatheringClient.findById(1L)).willReturn(Optional.empty());

            CreateReviewCommand command = new CreateReviewCommand(1L, 10L, List.of(
                    new CreateReviewCommand.ReviewItem(20L, List.of("성실해요"), null)
            ));

            assertThatThrownBy(() -> reviewCommandService.createReviews(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.GATHERING_NOT_COMPLETED);
        }

        @Test
        @DisplayName("이미 리뷰를 작성했으면 예외가 발생한다")
        void alreadySubmitted() {
            given(gatheringClient.findById(1L)).willReturn(Optional.of(completedGatheringInfo()));
            given(reviewRepository.existsByGatheringIdAndReviewerId(1L, 10L)).willReturn(true);

            CreateReviewCommand command = new CreateReviewCommand(1L, 10L, List.of(
                    new CreateReviewCommand.ReviewItem(20L, List.of("성실해요"), null)
            ));

            assertThatThrownBy(() -> reviewCommandService.createReviews(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.REVIEW_ALREADY_SUBMITTED);
        }

        @Test
        @DisplayName("태그 수에 비례하여 UserClient.addReputationScore가 호출된다")
        void reputationScoreViaClient() {
            given(gatheringClient.findById(1L)).willReturn(Optional.of(completedGatheringInfo()));
            given(reviewRepository.existsByGatheringIdAndReviewerId(1L, 10L)).willReturn(false);
            given(reviewRepository.save(any(Review.class))).willAnswer(inv -> inv.getArgument(0));

            CreateReviewCommand command = new CreateReviewCommand(1L, 10L, List.of(
                    new CreateReviewCommand.ReviewItem(20L, List.of("성실해요", "소통이 좋아요", "도움이 돼요"), null)
            ));

            reviewCommandService.createReviews(command);

            then(userClient).should().addReputationScore(20L, BigDecimal.valueOf(0.3));
        }
    }

    private GatheringInfo completedGatheringInfo() {
        return GatheringInfo.builder()
                .id(1L).title("Test Gathering").status(GatheringStatus.COMPLETED).build();
    }
}
