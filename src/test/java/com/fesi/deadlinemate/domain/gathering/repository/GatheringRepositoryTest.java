package com.fesi.deadlinemate.domain.gathering.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.global.config.JpaConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
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
}