package com.fesi.deadlinemate.domain.review.dto.request;

import com.fesi.deadlinemate.domain.review.command.CreateReviewCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateReviewRequest(
        @Valid @NotEmpty List<ReviewItem> reviews
) {
    public record ReviewItem(
            Long targetUserId,
            @NotEmpty List<String> tags,
            String comment
    ) {}

    public CreateReviewCommand toCommand(Long gatheringId, Long reviewerId) {
        return new CreateReviewCommand(
                gatheringId,
                reviewerId,
                reviews.stream()
                        .map(item -> new CreateReviewCommand.ReviewItem(
                                item.targetUserId(), item.tags(), item.comment()))
                        .toList()
        );
    }
}
