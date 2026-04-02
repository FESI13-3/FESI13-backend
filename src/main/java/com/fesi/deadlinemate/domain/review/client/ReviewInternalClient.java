package com.fesi.deadlinemate.domain.review.client;

import com.fesi.deadlinemate.domain.review.client.dto.ApplicantReviewInfo;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.entity.ReviewTag;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewInternalClient implements ReviewClient {

    private final ReviewRepository reviewRepository;

    @Override
    public Map<Long, ApplicantReviewInfo> getApplicantReviewInfos(List<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctTargetUserIds = targetUserIds.stream()
                .distinct()
                .toList();

        List<Review> reviews = reviewRepository.findByTargetUserIdInOrderByCreatedAtDesc(distinctTargetUserIds);

        Map<Long, List<Review>> reviewsByTargetUserId = reviews.stream()
                .collect(Collectors.groupingBy(Review::getTargetUserId));

        return distinctTargetUserIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        targetUserId -> toApplicantReviewInfo(
                                reviewsByTargetUserId.getOrDefault(targetUserId, List.of())
                        )
                ));
    }

    private ApplicantReviewInfo toApplicantReviewInfo(List<Review> reviews) {
        Map<String, Long> tagCounts = reviews.stream()
                .flatMap(review -> review.getTags().stream())
                .map(ReviewTag::getDisplayName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<String> topTags = tagCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(2)
                .map(Map.Entry::getKey)
                .toList();

        List<ApplicantReviewInfo.RecentReview> recentReviews = reviews.stream()
                .limit(2)
                .map(this::toRecentReview)
                .toList();

        return ApplicantReviewInfo.builder()
                .reviewCount(reviews.size())
                .topTags(topTags)
                .recentReviews(recentReviews)
                .build();
    }

    private ApplicantReviewInfo.RecentReview toRecentReview(Review review) {
        return ApplicantReviewInfo.RecentReview.builder()
                .id(review.getId())
                .comment(review.getComment())
                .tags(review.getTags().stream()
                        .map(ReviewTag::getDisplayName)
                        .toList())
                .build();
    }
}
