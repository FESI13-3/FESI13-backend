package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringRepositoryCustom {

    List<Gathering> findByIdIn(List<Long> ids);

    Page<Gathering> findByIdInOrderByCreatedAtDesc(List<Long> ids, Pageable pageable);

    Page<Gathering> findByIdInOrderByCreatedAtAsc(List<Long> ids, Pageable pageable);

    Page<Gathering> findByIdInAndStatusOrderByCreatedAtDesc(List<Long> ids, GatheringStatus status, Pageable pageable);

    Page<Gathering> findByIdInAndStatusOrderByCreatedAtAsc(List<Long> ids, GatheringStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Gathering g SET g.currentMembers = g.currentMembers - 1 WHERE g.id = :id AND g.currentMembers > 0")
    int decreaseCurrentMembers(@Param("id") Long gatheringId);

    List<Gathering> findByStatusAndStartDateLessThanEqual(GatheringStatus status, LocalDate startDate);

    List<Gathering> findByStatusAndEndDateLessThanEqual(GatheringStatus status, LocalDate endDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from Gathering g where g.id = :gatheringId")
    Optional<Gathering> findByIdForUpdate(@Param("gatheringId") Long gatheringId);
}
