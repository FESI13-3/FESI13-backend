package com.fesi.deadlinemate.domain.review.dto.response;

import com.fesi.deadlinemate.domain.review.entity.MatesTag;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.entity.ReviewTag;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record ReviewListResponse(
        List<ReviewItem> reviews,
        long totalCount,
        List<MatesTagCount> matesTagCounts
) {
    @Builder
    public record ReviewItem(
            Long id,
            Long gatheringId,
            String gatheringTitle,
            Long reviewerId,
            String reviewerNickname,
            String reviewerProfileImage,
            List<String> tags,
            String matesTag,
            String comment,
            LocalDateTime createdAt
    ) {}

    public record MatesTagCount(
            String tag,
            long count
    ) {}

    public static ReviewListResponse of(List<Review> reviews, long totalCount,
                                         Map<Long, UserInfo> reviewerMap,
                                         Map<Long, String> gatheringTitleMap,
                                         List<MatesTagCount> matesTagCounts) {
        List<ReviewItem> items = reviews.stream()
                .map(review -> {
                    UserInfo reviewer = reviewerMap.get(review.getReviewerId());
                    return ReviewItem.builder()
                            .id(review.getId())
                            .gatheringId(review.getGatheringId())
                            .gatheringTitle(gatheringTitleMap.getOrDefault(review.getGatheringId(), null))
                            .reviewerId(review.getReviewerId())
                            .reviewerNickname(reviewer != null ? reviewer.getNickname() : null)
                            .reviewerProfileImage(reviewer != null ? reviewer.getProfileImage() : null)
                            .tags(review.getTags().stream().map(ReviewTag::getDisplayName).toList())
                            .matesTag(review.getMatesTag() != null ? review.getMatesTag().getDisplayName() : null)
                            .comment(review.getComment())
                            .createdAt(review.getCreatedAt())
                            .build();
                })
                .toList();

        return ReviewListResponse.builder()
                .reviews(items)
                .totalCount(totalCount)
                .matesTagCounts(matesTagCounts)
                .build();
    }
}
