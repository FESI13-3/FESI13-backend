package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {
    boolean existsByGatheringIdAndUserIdAndIsActiveTrue(Long gatheringId, Long userId);
    void deleteByGatheringId(Long gatheringId);
    
    List<GatheringMember> findByGatheringIdAndIsActiveTrueOrderByIdAsc(Long gatheringId);

    Optional<GatheringMember> findByGatheringIdAndUserId(Long gatheringId, Long userId);

    Optional<GatheringMember> findByGatheringIdAndUserIdAndIsActiveTrue(Long gatheringId, Long userId);

    @Query("SELECT gm.gatheringId FROM GatheringMember gm WHERE gm.userId = :userId AND gm.isActive = true")
    List<Long> findActiveGatheringIdsByUserId(@Param("userId") Long userId);

    List<GatheringMember> findByGatheringIdInAndUserIdAndIsActiveTrue(List<Long> gatheringIds, Long userId);
}
