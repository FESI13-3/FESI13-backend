package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering, Long>, GatheringRepositoryCustom {

    Page<Gathering> findByIdInOrderByCreatedAtDesc(List<Long> ids, Pageable pageable);

    Page<Gathering> findByIdInAndStatusOrderByCreatedAtDesc(List<Long> ids, GatheringStatus status, Pageable pageable);
}
