package com.fesi.deadlinemate.domain.review.dto.request;

import com.fesi.deadlinemate.domain.review.command.CreateReviewCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateReviewRequest(
        @Valid @NotEmpty List<ReviewItem> reviews
) {
    public record ReviewItem(
            @NotNull Long targetUserId,
            @NotEmpty List<String> tags,
            String matesTag,
            String comment
    ) {}

    public CreateReviewCommand toCommand(Long gatheringId, Long reviewerId) {
        return new CreateReviewCommand(
                gatheringId,
                reviewerId,
                reviews.stream()
                        .map(item -> new CreateReviewCommand.ReviewItem(
                                item.targetUserId(), item.tags(), item.matesTag(), item.comment()))
                        .toList()
        );
    }
}
