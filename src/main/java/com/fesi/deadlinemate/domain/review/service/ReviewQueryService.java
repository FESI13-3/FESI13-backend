package com.fesi.deadlinemate.domain.review.service;

import com.fesi.deadlinemate.domain.gathering.client.GatheringClient;
import com.fesi.deadlinemate.domain.review.dto.response.ReviewListResponse;
import com.fesi.deadlinemate.domain.review.dto.response.ReviewListResponse.MatesTagCount;
import com.fesi.deadlinemate.domain.review.entity.MatesTag;
import com.fesi.deadlinemate.domain.review.entity.Review;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;
    private final GatheringClient gatheringClient;
    private final UserClient userClient;

    public ReviewListResponse getReviews(Long targetUserId, int page) {
        int validatedPage = Math.max(page, 1);
        Page<Review> result = reviewRepository.findByTargetUserIdOrderByCreatedAtDesc(
                targetUserId, PageRequest.of(validatedPage - 1, 10)
        );

        List<Long> reviewerIds = result.getContent().stream()
                .map(Review::getReviewerId).distinct().toList();
        Map<Long, UserInfo> reviewerMap = userClient.findByIds(reviewerIds);

        List<Long> gatheringIds = result.getContent().stream()
                .map(Review::getGatheringId).distinct().toList();
        Map<Long, String> gatheringTitleMap = gatheringClient.findTitlesByIds(gatheringIds);

        List<MatesTagCount> matesTagCounts = reviewRepository.countMatesTagsByTargetUserId(targetUserId)
                .stream()
                .map(row -> new MatesTagCount(((MatesTag) row[0]).getDisplayName(), (long) row[1]))
                .toList();

        return ReviewListResponse.of(result.getContent(), result.getTotalElements(),
                reviewerMap, gatheringTitleMap, matesTagCounts);
    }
}
