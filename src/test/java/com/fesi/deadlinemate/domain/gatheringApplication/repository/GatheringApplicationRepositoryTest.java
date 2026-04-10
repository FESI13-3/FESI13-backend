package com.fesi.deadlinemate.domain.gatheringApplication.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.GatheringApplication;
import com.fesi.deadlinemate.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class})
class GatheringApplicationRepositoryTest {

    @Autowired
    private GatheringApplicationRepository gatheringApplicationRepository;

    @Autowired
    private EntityManager em;

    private GatheringApplication application1;
    private GatheringApplication application2;
    private GatheringApplication application3;

    @BeforeEach
    void setUp() {
        application1 = saveApplication(1L, 101L, "목표1", "소개1", ApplicationStatus.PENDING,
                LocalDateTime.of(2026, 4, 10, 10, 0));
        application2 = saveApplication(1L, 102L, "목표2", "소개2", ApplicationStatus.ACCEPTED,
                LocalDateTime.of(2026, 4, 10, 11, 0));
        application3 = saveApplication(2L, 101L, "목표3", "소개3", ApplicationStatus.PENDING,
                LocalDateTime.of(2026, 4, 10, 12, 0));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("existsByGatheringIdAndApplicantId")
    class ExistsByGatheringIdAndApplicantId {

        @Test
        @DisplayName("특정 모임에 특정 신청자가 신청했으면 true를 반환한다")
        void exists_success() {
            boolean result = gatheringApplicationRepository.existsByGatheringIdAndApplicantId(1L, 101L);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findByGatheringIdAndApplicantId")
    class FindByGatheringIdAndApplicantId {

        @Test
        @DisplayName("특정 모임과 신청자로 신청 정보를 조회할 수 있다")
        void find_success() {
            Optional<GatheringApplication> result =
                    gatheringApplicationRepository.findByGatheringIdAndApplicantId(1L, 101L);

            assertThat(result).isPresent();
            assertThat(result.get().getGatheringId()).isEqualTo(1L);
            assertThat(result.get().getApplicantId()).isEqualTo(101L);
            assertThat(result.get().getStatus()).isEqualTo(ApplicationStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("findByGatheringIdOrderByCreatedAtAsc")
    class FindByGatheringIdOrderByCreatedAtAsc {

        @Test
        @DisplayName("같은 모임의 신청 목록을 생성일시 오름차순으로 조회한다")
        void findAllByGatheringIdOrderByCreatedAtAsc_success() {
            List<GatheringApplication> result =
                    gatheringApplicationRepository.findByGatheringIdOrderByCreatedAtAsc(1L);

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(GatheringApplication::getApplicantId)
                    .containsExactly(101L, 102L);
        }
    }

    @Nested
    @DisplayName("countByGatheringIdInAndStatus")
    class CountByGatheringIdInAndStatus {

        @Test
        @DisplayName("모임별 특정 상태의 신청 수를 집계한다")
        void countByGatheringIdInAndStatus_success() {
            List<Object[]> result = gatheringApplicationRepository.countByGatheringIdInAndStatus(
                    List.of(1L, 2L),
                    ApplicationStatus.PENDING
            );

            assertThat(result).hasSize(2);

            assertThat(result)
                    .anySatisfy(row -> {
                        assertThat(row[0]).isEqualTo(1L);
                        assertThat(row[1]).isEqualTo(1L);
                    })
                    .anySatisfy(row -> {
                        assertThat(row[0]).isEqualTo(2L);
                        assertThat(row[1]).isEqualTo(1L);
                    });
        }
    }

    private GatheringApplication saveApplication(
            Long gatheringId,
            Long applicantId,
            String personalGoal,
            String selfIntroduction,
            ApplicationStatus status,
            LocalDateTime createdAt
    ) {
        GatheringApplication application = GatheringApplication.builder()
                .gatheringId(gatheringId)
                .applicantId(applicantId)
                .personalGoal(personalGoal)
                .selfIntroduction(selfIntroduction)
                .status(status)
                .build();

        GatheringApplication saved = gatheringApplicationRepository.save(application);
        setCreatedAt(saved, createdAt);
        return saved;
    }

    private void setCreatedAt(Object target, LocalDateTime createdAt) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("createdAt");
                field.setAccessible(true);
                field.set(target, createdAt);
                return;
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("createdAt field not found");
    }
}