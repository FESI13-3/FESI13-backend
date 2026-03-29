package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {
    void deleteByGatheringId(Long gatheringId);
    List<GatheringMember> findByGatheringIdAndIsActiveTrueOrderByIdAsc(Long gatheringId);
    Optional<GatheringMember> findByGatheringIdAndUserIdAndIsActiveTrue(Long gatheringId, Long userId);
}
