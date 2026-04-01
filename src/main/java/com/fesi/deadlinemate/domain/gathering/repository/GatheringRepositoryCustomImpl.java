package com.fesi.deadlinemate.domain.gathering.repository;

import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.QGathering;
import com.fesi.deadlinemate.domain.gathering.entity.QGatheringTag;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringDetailRow;
import com.fesi.deadlinemate.domain.gathering.projection.GatheringListRow;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GatheringRepositoryCustomImpl implements GatheringRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GatheringListRow> search(GatheringSearchCondition condition, Pageable pageable) {
        QGathering gathering = QGathering.gathering;
        QGatheringTag gatheringTag = QGatheringTag.gatheringTag;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.type() != null) {
            builder.and(gathering.type.eq(condition.type()));
        }

        if (condition.normalizedCategory() != null) {
            builder.and(gathering.category.eq(condition.normalizedCategory()));
        }

        if ("recruiting".equals(condition.normalizedStatus())) {
            builder.and(gathering.status.eq(GatheringStatus.RECRUITING));
        }

        if (condition.normalizedQuery() != null) {
            String keyword = condition.normalizedQuery();

            builder.and(
                    gathering.title.containsIgnoreCase(keyword)
                            .or(gathering.shortDescription.containsIgnoreCase(keyword))
                            .or(
                                    JPAExpressions
                                            .selectOne()
                                            .from(gatheringTag)
                                            .where(
                                                    gatheringTag.gatheringId.eq(gathering.id),
                                                    gatheringTag.tag.containsIgnoreCase(keyword)
                                            )
                                            .exists()
                            )
            );
        }

        List<GatheringListRow> content = queryFactory
                .select(Projections.constructor(
                        GatheringListRow.class,
                        gathering.id,
                        gathering.leaderId,
                        gathering.type,
                        gathering.category,
                        gathering.title,
                        gathering.shortDescription,
                        gathering.maxMembers,
                        gathering.currentMembers,
                        gathering.recruitDeadline,
                        gathering.startDate,
                        gathering.endDate,
                        gathering.status
                ))
                .from(gathering)
                .where(builder)
                .orderBy(resolveOrderSpecifiers(condition.normalizedSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(gathering.count())
                .from(gathering)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public List<GatheringListRow> findMainPopular(int limit) {
        QGathering gathering = QGathering.gathering;

        return queryFactory
                .select(Projections.constructor(
                        GatheringListRow.class,
                        gathering.id,
                        gathering.leaderId,
                        gathering.type,
                        gathering.category,
                        gathering.title,
                        gathering.shortDescription,
                        gathering.maxMembers,
                        gathering.currentMembers,
                        gathering.recruitDeadline,
                        gathering.startDate,
                        gathering.endDate,
                        gathering.status
                ))
                .from(gathering)
                .where(gathering.status.eq(GatheringStatus.RECRUITING))
                .orderBy(gathering.viewCount.desc(), gathering.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<GatheringListRow> findMainDeadline(int limit) {
        QGathering gathering = QGathering.gathering;

        return queryFactory
                .select(Projections.constructor(
                        GatheringListRow.class,
                        gathering.id,
                        gathering.leaderId,
                        gathering.type,
                        gathering.category,
                        gathering.title,
                        gathering.shortDescription,
                        gathering.maxMembers,
                        gathering.currentMembers,
                        gathering.recruitDeadline,
                        gathering.startDate,
                        gathering.endDate,
                        gathering.status
                ))
                .from(gathering)
                .where(gathering.status.eq(GatheringStatus.RECRUITING))
                .orderBy(gathering.recruitDeadline.asc(), gathering.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<GatheringListRow> findMainLatest(int limit) {
        QGathering gathering = QGathering.gathering;

        return queryFactory
                .select(Projections.constructor(
                        GatheringListRow.class,
                        gathering.id,
                        gathering.leaderId,
                        gathering.type,
                        gathering.category,
                        gathering.title,
                        gathering.shortDescription,
                        gathering.maxMembers,
                        gathering.currentMembers,
                        gathering.recruitDeadline,
                        gathering.startDate,
                        gathering.endDate,
                        gathering.status
                ))
                .from(gathering)
                .where(gathering.status.eq(GatheringStatus.RECRUITING))
                .orderBy(gathering.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<GatheringDetailRow> findDetailRowById(Long gatheringId) {
        QGathering gathering = QGathering.gathering;

        GatheringDetailRow row = queryFactory
                .select(Projections.constructor(
                        GatheringDetailRow.class,
                        gathering.id,
                        gathering.leaderId,
                        gathering.type,
                        gathering.category,
                        gathering.title,
                        gathering.shortDescription,
                        gathering.description,
                        gathering.goal,
                        gathering.maxMembers,
                        gathering.currentMembers,
                        gathering.recruitDeadline,
                        gathering.startDate,
                        gathering.endDate,
                        gathering.totalWeeks,
                        gathering.status
                ))
                .from(gathering)
                .where(gathering.id.eq(gatheringId))
                .fetchOne();

        return Optional.ofNullable(row);
    }

    @Override
    public Page<GatheringListRow> findByIdIn(List<Long> gatheringIds, Pageable pageable) {
        QGathering gathering = QGathering.gathering;

        if (gatheringIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        List<GatheringListRow> content = queryFactory
                .select(Projections.constructor(
                        GatheringListRow.class,
                        gathering.id,
                        gathering.leaderId,
                        gathering.type,
                        gathering.category,
                        gathering.title,
                        gathering.shortDescription,
                        gathering.maxMembers,
                        gathering.currentMembers,
                        gathering.recruitDeadline,
                        gathering.startDate,
                        gathering.endDate,
                        gathering.status
                ))
                .from(gathering)
                .where(gathering.id.in(gatheringIds))
                .orderBy(gathering.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(gathering.count())
                .from(gathering)
                .where(gathering.id.in(gatheringIds))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private OrderSpecifier<?>[] resolveOrderSpecifiers(String sort) {
        QGathering gathering = QGathering.gathering;

        return switch (sort) {
            case "popular" -> new OrderSpecifier[]{
                    gathering.viewCount.desc(),
                    gathering.createdAt.desc()
            };
            case "deadline" -> new OrderSpecifier[]{
                    gathering.recruitDeadline.asc(),
                    gathering.createdAt.desc()
            };
            default -> new OrderSpecifier[]{
                    gathering.createdAt.desc()
            };
        };
    }
}