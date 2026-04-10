package com.fesi.deadlinemate.domain.gathering.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringDetailRow;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringListRow;
import com.fesi.deadlinemate.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
class GatheringRepositoryTest {

    @Autowired
    private GatheringRepository gatheringRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("모임을 저장하고 조회할 수 있다")
    void saveAndFind() {
        // given
        Gathering gathering = Gathering.builder()
                .leaderId(1L)
                .type(GatheringType.STUDY)
                .title("React 완전 정복 스터디")
                .shortDescription("리액트 공식문서를 같이 읽어요")
                .description("매주 공식문서 1챕터씩 읽고 블로그를 작성합니다...")
                .goal("React 공식문서 완독 + 블로그 5편 작성")
                .maxMembers(6)
                .currentMembers(1)
                .recruitDeadline(LocalDate.of(2025, 3, 20))
                .startDate(LocalDate.of(2025, 3, 22))
                .endDate(LocalDate.of(2025, 4, 19))
                .totalWeeks(5)
                .status(GatheringStatus.RECRUITING)
                .viewCount(0)
                .build();

        // when
        Gathering saved = gatheringRepository.save(gathering);

        // then
        assertThat(saved.getId()).isNotNull();

        Gathering found = gatheringRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getLeaderId()).isEqualTo(1L);
        assertThat(found.getType()).isEqualTo(GatheringType.STUDY);
        assertThat(found.getTitle()).isEqualTo("React 완전 정복 스터디");
        assertThat(found.getStatus()).isEqualTo(GatheringStatus.RECRUITING);
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("기본 검색은 recruiting 상태 모임만 최신순으로 조회한다")
        void search_defaultCondition_returnsRecruitingOnly() {
            // given
            Gathering recruiting1 = gatheringRepository.save(createGathering(
                    1L, GatheringType.STUDY, "스터디 A", "설명 A", "상세 설명 AAAAA", "목표 A",
                    5, 1,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 10
            ));
            Gathering inProgress = gatheringRepository.save(createGathering(
                    2L, GatheringType.PROJECT, "프로젝트 B", "설명 B", "상세 설명 BBBBB", "목표 B",
                    5, 2,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.IN_PROGRESS, 50
            ));
            Gathering recruiting2 = gatheringRepository.save(createGathering(
                    3L, GatheringType.STUDY, "스터디 C", "설명 C", "상세 설명 CCCCC", "목표 C",
                    5, 1,
                    LocalDate.of(2025, 3, 12),
                    LocalDate.of(2025, 3, 13),
                    LocalDate.of(2025, 4, 2),
                    4, GatheringStatus.RECRUITING, 20
            ));

            em.flush();
            em.clear();

            GatheringSearchCondition condition = GatheringSearchCondition.builder().build();

            // when
            var result = gatheringRepository.search(condition, org.springframework.data.domain.PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(GatheringListRow::id)
                    .containsExactly(recruiting2.getId(), recruiting1.getId());
            assertThat(result.getContent())
                    .extracting(GatheringListRow::id)
                    .doesNotContain(inProgress.getId());
        }

        @Test
        @DisplayName("sort가 popular면 viewCount 내림차순으로 조회한다")
        void search_sortPopular() {
            // given
            Gathering low = gatheringRepository.save(createGathering(
                    1L, GatheringType.STUDY, "모임1", "설명", "상세 설명 AAAAA", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 10
            ));
            Gathering high = gatheringRepository.save(createGathering(
                    2L, GatheringType.STUDY, "모임2", "설명", "상세 설명 BBBBB", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 100
            ));

            em.flush();
            em.clear();

            GatheringSearchCondition condition = GatheringSearchCondition.builder()
                    .sort("popular")
                    .status("all")
                    .build();

            // when
            var result = gatheringRepository.search(condition, org.springframework.data.domain.PageRequest.of(0, 10));

            // then
            assertThat(result.getContent())
                    .extracting(GatheringListRow::id)
                    .containsExactly(high.getId(), low.getId());
        }
    }

    @Nested
    @DisplayName("main query")
    class MainQuery {

        @Test
        @DisplayName("findMainPopular는 recruiting 상태만 viewCount 내림차순으로 조회한다")
        void findMainPopular_success() {
            // given
            Gathering high = gatheringRepository.save(createGathering(
                    1L, GatheringType.STUDY, "인기 모임", "설명", "상세 설명 AAAAA", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 20),
                    LocalDate.of(2025, 3, 21),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 100
            ));
            Gathering low = gatheringRepository.save(createGathering(
                    2L, GatheringType.STUDY, "덜 인기 모임", "설명", "상세 설명 BBBBB", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 20),
                    LocalDate.of(2025, 3, 21),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 10
            ));
            gatheringRepository.save(createGathering(
                    3L, GatheringType.STUDY, "진행중 모임", "설명", "상세 설명 CCCCC", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 20),
                    LocalDate.of(2025, 3, 21),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.IN_PROGRESS, 1000
            ));

            em.flush();
            em.clear();

            // when
            List<GatheringListRow> result = gatheringRepository.findMainPopular(10);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(GatheringListRow::id)
                    .containsExactly(high.getId(), low.getId());
        }

        @Test
        @DisplayName("findMainLatest는 recruiting 상태만 최신순으로 조회한다")
        void findMainLatest_success() {
            // given
            Gathering first = gatheringRepository.save(createGathering(
                    1L, GatheringType.STUDY, "먼저 저장", "설명", "상세 설명 AAAAA", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 20),
                    LocalDate.of(2025, 3, 21),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 10
            ));
            Gathering second = gatheringRepository.save(createGathering(
                    2L, GatheringType.STUDY, "나중 저장", "설명", "상세 설명 BBBBB", "목표",
                    5, 1,
                    LocalDate.of(2025, 3, 20),
                    LocalDate.of(2025, 3, 21),
                    LocalDate.of(2025, 4, 1),
                    4, GatheringStatus.RECRUITING, 10
            ));

            em.flush();
            em.clear();

            // when
            List<GatheringListRow> result = gatheringRepository.findMainLatest(10);

            // then
            assertThat(result).extracting(GatheringListRow::id)
                    .containsExactly(second.getId(), first.getId());
        }
    }

    @Nested
    @DisplayName("detail and id queries")
    class DetailAndIdQueries {

        @Test
        @DisplayName("findDetailRowById는 상세 조회용 row를 반환한다")
        void findDetailRowById_success() {
            // given
            Gathering saved = gatheringRepository.save(createGathering(
                    1L, GatheringType.PROJECT, "프로젝트 모임", "짧은 설명", "상세 설명 AAAAA", "최종 목표",
                    6, 2,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 10),
                    5, GatheringStatus.RECRUITING, 30
            ));

            em.flush();
            em.clear();

            // when
            Optional<GatheringDetailRow> result = gatheringRepository.findDetailRowById(saved.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(saved.getId());
            assertThat(result.get().leaderId()).isEqualTo(1L);
            assertThat(result.get().type()).isEqualTo(GatheringType.PROJECT);
            assertThat(result.get().title()).isEqualTo("프로젝트 모임");
            assertThat(result.get().goal()).isEqualTo("최종 목표");
            assertThat(result.get().status()).isEqualTo(GatheringStatus.RECRUITING);
        }

        @Test
        @DisplayName("findByIdIn은 빈 ID 목록이면 빈 페이지를 반환한다")
        void findByIdIn_emptyIds() {
            // when
            var result = gatheringRepository.findByIdIn(
                    List.of(),
                    org.springframework.data.domain.PageRequest.of(0, 10)
            );

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();
        }
    }

    @Nested
    @DisplayName("update and date queries")
    class UpdateAndDateQueries {

        @Test
        @DisplayName("decreaseCurrentMembers는 현재 인원을 1 감소시킨다")
        void decreaseCurrentMembers_success() {
            // given
            Gathering saved = gatheringRepository.save(createGathering(
                    1L, GatheringType.STUDY, "멤버 감소 테스트", "설명", "상세 설명 AAAAA", "목표",
                    5, 3,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 10),
                    5, GatheringStatus.RECRUITING, 10
            ));
            em.flush();
            em.clear();

            // when
            int affected = gatheringRepository.decreaseCurrentMembers(saved.getId());
            em.flush();
            em.clear();

            // then
            assertThat(affected).isEqualTo(1);

            Gathering found = gatheringRepository.findById(saved.getId()).orElseThrow();
            assertThat(found.getCurrentMembers()).isEqualTo(2);
        }

        @Test
        @DisplayName("currentMembers가 0이면 decreaseCurrentMembers는 변경하지 않는다")
        void decreaseCurrentMembers_whenZero() {
            // given
            Gathering saved = gatheringRepository.save(createGathering(
                    1L, GatheringType.STUDY, "0명 테스트", "설명", "상세 설명 AAAAA", "목표",
                    5, 0,
                    LocalDate.of(2025, 3, 10),
                    LocalDate.of(2025, 3, 11),
                    LocalDate.of(2025, 4, 10),
                    5, GatheringStatus.RECRUITING, 10
            ));
            em.flush();
            em.clear();

            // when
            int affected = gatheringRepository.decreaseCurrentMembers(saved.getId());
            em.flush();
            em.clear();

            // then
            assertThat(affected).isZero();

            Gathering found = gatheringRepository.findById(saved.getId()).orElseThrow();
            assertThat(found.getCurrentMembers()).isZero();
        }
    }

    private Gathering createGathering(
            Long leaderId,
            GatheringType type,
            String title,
            String shortDescription,
            String description,
            String goal,
            int maxMembers,
            int currentMembers,
            LocalDate recruitDeadline,
            LocalDate startDate,
            LocalDate endDate,
            int totalWeeks,
            GatheringStatus status,
            int viewCount
    ) {
        return Gathering.builder()
                .leaderId(leaderId)
                .type(type)
                .title(title)
                .shortDescription(shortDescription)
                .description(description)
                .goal(goal)
                .maxMembers(maxMembers)
                .currentMembers(currentMembers)
                .recruitDeadline(recruitDeadline)
                .startDate(startDate)
                .endDate(endDate)
                .totalWeeks(totalWeeks)
                .status(status)
                .viewCount(viewCount)
                .build();
    }
}