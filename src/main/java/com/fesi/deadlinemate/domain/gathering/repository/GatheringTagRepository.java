package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringTagRepository extends JpaRepository<GatheringTag,Long> {
    void deleteByGatheringId(Long gatheringId);
    List<GatheringTag> findByGatheringIdOrderByIdAsc(Long gatheringId);
}
