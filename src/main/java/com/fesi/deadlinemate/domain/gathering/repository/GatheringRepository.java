package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringRepository extends JpaRepository<Gathering,Long>,GatheringRepositoryCustom {

    List<Gathering> findByIdIn(List<Long> ids);
}
