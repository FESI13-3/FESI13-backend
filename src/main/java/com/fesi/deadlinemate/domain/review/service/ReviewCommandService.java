package com.fesi.deadlinemate.domain.review.service;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.review.command.CreateReviewCommand;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandService {

    private static final BigDecimal REPUTATION_DELTA_PER_TAG = BigDecimal.valueOf(0.1);

    private final ReviewRepository reviewRepository;
    private final GatheringClient gatheringClient;
    private final UserClient userClient;

    public void createReviews(CreateReviewCommand command) {
        validateGatheringCompleted(command.gatheringId());

        if (reviewRepository.existsByGatheringIdAndReviewerId(command.gatheringId(), command.reviewerId())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_SUBMITTED);
        }

        command.reviews().forEach(item -> {
            Review review = Review.create(
                    command.gatheringId(), command.reviewerId(),
                    item.targetUserId(), item.tags(), item.comment()
            );

            reviewRepository.save(review);

            BigDecimal delta = REPUTATION_DELTA_PER_TAG.multiply(BigDecimal.valueOf(review.getTagCount()));
            userClient.addReputationScore(item.targetUserId(), delta);
        });
    }

    private void validateGatheringCompleted(Long gatheringId) {
        gatheringClient.findById(gatheringId)
                .filter(info -> info.isCompleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_COMPLETED));
    }
}
