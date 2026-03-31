package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringRepository extends JpaRepository<Gathering,Long>,GatheringRepositoryCustom {
    List<Gathering> findByStatusAndEndDateLessThanEqual(GatheringStatus status, LocalDate endDate);
  
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Gathering g where g.id = :gatheringId")
    Optional<Gathering> findByIdForUpdate(@Param("gatheringId") Long gatheringId);
}
