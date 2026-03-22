package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {
    void deleteByGatheringId(Long gatheringId);
}
