package com.fesi.deadlinemate.domain.gathering.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.global.config.JpaConfig;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Import(JpaConfig.class)
class GatheringMemberRepositoryTest {

    @Autowired
    private GatheringMemberRepository gatheringMemberRepository;

    @Autowired
    private GatheringRepository gatheringRepository;

    private Long gatheringId;
    private Long gathering2Id;

    @BeforeEach
    void setUp() {
        gatheringMemberRepository.deleteAll();
        gatheringRepository.deleteAll();

        Gathering g1 = gatheringRepository.save(gathering("스터디 1"));
        Gathering g2 = gatheringRepository.save(gathering("스터디 2"));
        gatheringId = g1.getId();
        gathering2Id = g2.getId();

        // gathering1: user1(LEADER, active), user2(MEMBER, active), user3(MEMBER, inactive)
        gatheringMemberRepository.saveAll(List.of(
                member(gatheringId, 1L, GatheringRole.LEADER, true),
                member(gatheringId, 2L, GatheringRole.MEMBER, true),
                member(gatheringId, 3L, GatheringRole.MEMBER, false)
        ));

        // gathering2: user1(MEMBER, active)
        gatheringMemberRepository.save(member(gathering2Id, 1L, GatheringRole.MEMBER, true));
    }

    @Nested
    @DisplayName("활성 멤버 목록 조회")
    class FindActiveMembers {

        @Test
        @DisplayName("isActive=true인 멤버만 조회한다")
        void findOnlyActiveMembers() {
            // when
            List<GatheringMember> members = gatheringMemberRepository
                    .findByGatheringIdAndIsActiveTrueOrderByIdAsc(gatheringId);

            // then
            assertThat(members).hasSize(2);
            assertThat(members).allMatch(GatheringMember::isActive);
        }

        @Test
        @DisplayName("id 오름차순으로 정렬된다")
        void orderedById() {
            // when
            List<GatheringMember> members = gatheringMemberRepository
                    .findByGatheringIdAndIsActiveTrueOrderByIdAsc(gatheringId);

            // then
            List<Long> ids = members.stream().map(GatheringMember::getId).toList();
            assertThat(ids).isSorted();
        }
    }

    @Nested
    @DisplayName("gatheringId + userId 단건 조회")
    class FindByGatheringAndUser {

        @Test
        @DisplayName("gatheringId와 userId로 멤버를 조회할 수 있다")
        void findByGatheringIdAndUserId() {
            // when
            Optional<GatheringMember> result = gatheringMemberRepository
                    .findByGatheringIdAndUserId(gatheringId, 1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getRole()).isEqualTo(GatheringRole.LEADER);
        }

        @Test
        @DisplayName("존재하지 않는 멤버는 빈 Optional을 반환한다")
        void notFound() {
            // when
            Optional<GatheringMember> result = gatheringMemberRepository
                    .findByGatheringIdAndUserId(gatheringId, 99L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("활성 멤버 존재 여부 확인")
    class ExistsActiveMembers {

        @Test
        @DisplayName("활성 멤버이면 true를 반환한다")
        void existsActiveMember() {
            // when
            boolean exists = gatheringMemberRepository
                    .existsByGatheringIdAndUserIdAndIsActiveTrue(gatheringId, 1L);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("비활성 멤버이면 false를 반환한다")
        void inactiveReturnsFalse() {
            // when
            boolean exists = gatheringMemberRepository
                    .existsByGatheringIdAndUserIdAndIsActiveTrue(gatheringId, 3L);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("userId로 활성 gatheringId 목록 조회")
    class FindActiveGatheringIds {

        @Test
        @DisplayName("user1이 활성 멤버인 모임 ID 목록을 반환한다")
        void findActiveGatheringIdsByUserId() {
            // when
            List<Long> ids = gatheringMemberRepository.findActiveGatheringIdsByUserId(1L);

            // then
            assertThat(ids).containsExactlyInAnyOrder(gatheringId, gathering2Id);
        }

        @Test
        @DisplayName("비활성 멤버인 모임은 포함되지 않는다")
        void inactiveMemberNotIncluded() {
            // when
            List<Long> ids = gatheringMemberRepository.findActiveGatheringIdsByUserId(3L);

            // then
            assertThat(ids).isEmpty();
        }
    }

    private Gathering gathering(String title) {
        return Gathering.builder()
                .leaderId(1L)
                .type(GatheringType.STUDY)
                .category("개발")
                .title(title)
                .shortDescription("설명")
                .description("상세 설명")
                .goal("목표")
                .maxMembers(6)
                .currentMembers(1)
                .recruitDeadline(LocalDate.of(2025, 3, 20))
                .startDate(LocalDate.of(2025, 3, 22))
                .endDate(LocalDate.of(2025, 4, 19))
                .totalWeeks(4)
                .status(GatheringStatus.IN_PROGRESS)
                .viewCount(0)
                .build();
    }

    private GatheringMember member(Long gatheringId, Long userId, GatheringRole role, boolean active) {
        return GatheringMember.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .role(role)
                .overallAchievementRate(BigDecimal.ZERO)
                .isActive(active)
                .build();
    }
}
