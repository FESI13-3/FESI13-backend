package com.fesi.deadlinemate.domain.like.repository;

import com.fesi.deadlinemate.domain.like.entity.GatheringLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringLikeRepository extends JpaRepository<GatheringLike, Long> {
    boolean existsByGatheringIdAndUserId(Long gatheringId, Long userId);
    Optional<GatheringLike> findByGatheringIdAndUserId(Long gatheringId, Long userId);
    void deleteByGatheringId(Long gatheringId);
    List<GatheringLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT gl.gatheringId FROM GatheringLike gl WHERE gl.userId = :userId ORDER BY gl.createdAt DESC")
    List<Long> findGatheringIdsByUserId(@Param("userId") Long userId);
}
