package com.fesi.deadlinemate.domain.review.repository;

import com.fesi.deadlinemate.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByGatheringIdAndReviewerId(Long gatheringId, Long reviewerId);

    Page<Review> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId, Pageable pageable);
}
