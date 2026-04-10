package com.fesi.deadlinemate.domain.category.repository;

import com.fesi.deadlinemate.domain.category.entity.GatheringCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringCategoryRepository extends JpaRepository<GatheringCategory, Long> {

    List<GatheringCategory> findByGatheringId(Long gatheringId);

    List<GatheringCategory> findByGatheringIdIn(List<Long> gatheringIds);

    void deleteByGatheringId(Long gatheringId);
}
