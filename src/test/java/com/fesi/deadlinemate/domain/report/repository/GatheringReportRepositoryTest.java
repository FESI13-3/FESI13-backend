package com.fesi.deadlinemate.domain.report.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.report.entity.GatheringReport;
import com.fesi.deadlinemate.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
class GatheringReportRepositoryTest {

    @Autowired
    private GatheringReportRepository gatheringReportRepository;

    @Autowired
    private EntityManager em;

    private GatheringReport report;

    @BeforeEach
    void setUp() {
        report = GatheringReport.builder()
                .gatheringId(1L)
                .teamOverallRate(BigDecimal.valueOf(80.0))
                .weeklyRates("[{\"week\":1,\"rate\":80.0}]")
                .build();

        gatheringReportRepository.save(report);
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("gatheringId로 리포트를 조회할 수 있다")
    void findByGatheringId_success() {
        Optional<GatheringReport> result =
                gatheringReportRepository.findByGatheringId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getGatheringId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 gatheringId면 empty를 반환한다")
    void findByGatheringId_notFound() {
        Optional<GatheringReport> result =
                gatheringReportRepository.findByGatheringId(999L);

        assertThat(result).isEmpty();
    }
}