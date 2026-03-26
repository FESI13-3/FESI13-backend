package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringDetailRow;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringListRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GatheringRepositoryCustom {
    Page<GatheringListRow> search(GatheringSearchCondition condition, Pageable pageable);
    List<GatheringListRow> findMainPopular(int limit);
    List<GatheringListRow> findMainDeadline(int limit);
    List<GatheringListRow> findMainLatest(int limit);
    Optional<GatheringDetailRow> findDetailRowById(Long gatheringId);
}
