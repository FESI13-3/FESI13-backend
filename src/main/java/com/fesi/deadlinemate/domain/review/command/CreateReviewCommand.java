package com.fesi.deadlinemate.domain.review.command;

import java.util.List;

public record CreateReviewCommand(
        Long gatheringId,
        Long reviewerId,
        List<ReviewItem> reviews
) {
    public record ReviewItem(
            Long targetUserId,
            List<String> tags,
            String comment
    ) {}
}
