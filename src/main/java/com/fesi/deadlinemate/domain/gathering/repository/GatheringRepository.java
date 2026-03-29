package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering,Long>,GatheringRepositoryCustom {
    List<Gathering> findByStatusAndEndDateLessThanEqual(GatheringStatus status, LocalDate endDate);
}
