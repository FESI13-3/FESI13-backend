package com.fesi.deadlinemate.domain.report.repository;

import com.fesi.deadlinemate.domain.report.entity.GatheringReport;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringReportRepository extends JpaRepository<GatheringReport, Long> {
    Optional<GatheringReport> findByGatheringId(Long gatheringId);
}
