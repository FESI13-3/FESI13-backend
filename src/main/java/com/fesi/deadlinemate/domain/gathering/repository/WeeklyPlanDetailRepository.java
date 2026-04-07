package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.WeeklyPlanDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyPlanDetailRepository extends JpaRepository<WeeklyPlanDetail, Long> {

    List<WeeklyPlanDetail> findByWeeklyPlanIdInOrderByWeeklyPlanIdAscDisplayOrderAsc(List<Long> weeklyPlanIds);

    void deleteByWeeklyPlanIdIn(List<Long> weeklyPlanIds);
}
