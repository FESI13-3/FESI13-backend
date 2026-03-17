package com.fesi.deadlinemate.domain.gathering.repository.mock;

import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.ApplicationEntity;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.GatheringEntity;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.TodoEntity;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class MockStore {public final Map<Long, GatheringEntity> gatherings = new ConcurrentHashMap<>();
    public final Map<Long, ApplicationEntity> applications = new ConcurrentHashMap<>();
    public final Map<Long, TodoEntity> todos = new ConcurrentHashMap<>();
    public final Map<Long, Set<Long>> gatheringMembers = new ConcurrentHashMap<>();
    public final Map<Long, Set<Long>> gatheringLikes = new ConcurrentHashMap<>();

    public final AtomicLong gatheringSeq = new AtomicLong(1);
    public final AtomicLong applicationSeq = new AtomicLong(1);
    public final AtomicLong todoSeq = new AtomicLong(1);

    @PostConstruct
    void init() {
        GatheringEntity g = new GatheringEntity();
        g.id = gatheringSeq.getAndIncrement();
        g.type = "스터디";
        g.category = "개발";
        g.title = "React 완전 정복 스터디";
        g.shortDescription = "리액트 공식문서를 같이 읽어요";
        g.description = "매주 공식문서 1챕터씩 읽고 블로그를 작성합니다.";
        g.tags = List.of("React", "프론트엔드");
        g.goal = "React 공식문서 완독 + 블로그 5편 작성";
        g.maxMembers = 6;
        g.currentMembers = 2;
        g.recruitDeadline = LocalDate.now().plusDays(5);
        g.startDate = LocalDate.now().plusDays(7);
        g.endDate = LocalDate.now().plusDays(35);
        g.status = "RECRUITING";
        g.leaderId = 1L;
        g.weeklyGuides = List.of(
                new com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.WeeklyGuideRequest(1, "JSX, 컴포넌트, Props", "공식문서 1~3챕터 읽기"),
                new com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.WeeklyGuideRequest(2, "State, 이벤트 처리", "공식문서 4~6챕터 읽기")
        );
        g.images = List.of(Map.of("url", "https://example.com/meeting1.jpg", "displayOrder", 0));
        g.createdAt = OffsetDateTime.now();

        gatherings.put(g.id, g);
        gatheringMembers.put(g.id, new LinkedHashSet<>(List.of(1L, 2L)));
        gatheringLikes.put(g.id, new LinkedHashSet<>(List.of(1L)));
    }
}
