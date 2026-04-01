package com.fesi.deadlinemate.domain.review.client.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ApplicantReviewInfo(
        long reviewCount,
        List<String> topTags,
        List<RecentReview> recentReviews
) {
    @Builder
    public static record RecentReview(
            Long id,
            String comment,
            List<String> tags
    ) {}
}


