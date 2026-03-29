package com.fesi.deadlinemate.domain.like.repository;

import com.fesi.deadlinemate.domain.like.entity.GatheringLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringLikeRepository extends JpaRepository<GatheringLike, Long> {
    boolean existsByGatheringIdAndUserId(Long gatheringId, Long userId);
    Optional<GatheringLike> findByGatheringIdAndUserId(Long gatheringId, Long userId);
    void deleteByGatheringId(Long gatheringId);
    List<GatheringLike> findByUserIdOrderByCreatedAtDesc(Long userId);
}
