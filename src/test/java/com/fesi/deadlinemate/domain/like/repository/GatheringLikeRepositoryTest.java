package com.fesi.deadlinemate.domain.like.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.like.entity.GatheringLike;
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
class GatheringLikeRepositoryTest {

    @Autowired
    private GatheringLikeRepository gatheringLikeRepository;

    @Autowired
    private EntityManager em;

    private GatheringLike like1;
    private GatheringLike like2;
    private GatheringLike like3;

    @BeforeEach
    void setUp() {
        like1 = saveLike(1L, 100L, LocalDateTime.of(2026, 4, 10, 10, 0));
        like2 = saveLike(2L, 100L, LocalDateTime.of(2026, 4, 10, 11, 0));
        like3 = saveLike(3L, 200L, LocalDateTime.of(2026, 4, 10, 12, 0));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("existsByGatheringIdAndUserId")
    class ExistsByGatheringIdAndUserId {

        @Test
        @DisplayName("특정 모임을 특정 사용자가 찜했으면 true를 반환한다")
        void exists_success() {
            boolean result = gatheringLikeRepository.existsByGatheringIdAndUserId(1L, 100L);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findByGatheringIdAndUserId")
    class FindByGatheringIdAndUserId {

        @Test
        @DisplayName("특정 모임과 사용자로 찜 정보를 조회할 수 있다")
        void find_success() {
            Optional<GatheringLike> result =
                    gatheringLikeRepository.findByGatheringIdAndUserId(2L, 100L);

            assertThat(result).isPresent();
            assertThat(result.get().getGatheringId()).isEqualTo(2L);
            assertThat(result.get().getUserId()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("findGatheringIdsByUserId")
    class FindGatheringIdsByUserId {

        @Test
        @DisplayName("사용자가 찜한 모임 ID를 createdAt 내림차순으로 조회한다")
        void findGatheringIdsByUserId_success() {
            List<Long> result = gatheringLikeRepository.findGatheringIdsByUserId(100L);

            assertThat(result).containsExactly(2L, 1L);
        }
    }

    private GatheringLike saveLike(Long gatheringId, Long userId, LocalDateTime createdAt) {
        GatheringLike like = GatheringLike.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .build();

        GatheringLike saved = gatheringLikeRepository.save(like);
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