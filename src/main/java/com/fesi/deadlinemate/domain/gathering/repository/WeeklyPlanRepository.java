package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, Long> {
    void deleteByGatheringId(Long gatheringId);
}
