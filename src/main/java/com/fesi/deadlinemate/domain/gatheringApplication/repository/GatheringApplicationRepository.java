package com.fesi.deadlinemate.domain.gatheringApplication.repository;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatheringApplicationRepository extends JpaRepository<GatheringApplication, Long> {

    boolean existsByGatheringIdAndApplicantId(Long gatheringId, Long applicantId);
    Optional<GatheringApplication> findByGatheringIdAndApplicantId(Long gatheringId, Long applicantId);
    Optional<GatheringApplication> findByIdAndGatheringId(Long applicationId, Long gatheringId);
    List<GatheringApplication> findByGatheringIdOrderByCreatedAtAsc(Long gatheringId);
    List<GatheringApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
}
