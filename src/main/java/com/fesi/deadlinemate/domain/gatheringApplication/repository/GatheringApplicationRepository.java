package com.fesi.deadlinemate.domain.gatheringApplication.repository;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GatheringApplicationRepository extends JpaRepository<GatheringApplication, Long> {

    boolean existsByGatheringIdAndApplicantId(Long gatheringId, Long applicantId);
    Optional<GatheringApplication> findByGatheringIdAndApplicantId(Long gatheringId, Long applicantId);
    Optional<GatheringApplication> findByIdAndGatheringId(Long applicationId, Long gatheringId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ga
        from GatheringApplication ga
        where ga.id = :applicationId
          and ga.gatheringId = :gatheringId
    """)
    Optional<GatheringApplication> findByIdAndGatheringIdForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("gatheringId") Long gatheringId
    );
    List<GatheringApplication> findByGatheringIdOrderByCreatedAtAsc(Long gatheringId);
    List<GatheringApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);
}
