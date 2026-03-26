package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringImageRepository extends JpaRepository<GatheringImage,Long> {
    List<GatheringImage> findByGatheringIdOrderByDisplayOrderAsc(Long gatheringId);
}
