package com.fesi.deadlinemate.domain.user.service;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.review.repository.ReviewRepository;
import com.fesi.deadlinemate.domain.todo.repository.TodoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatsQueryService {

    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringRepository gatheringRepository;
    private final TodoRepository todoRepository;
    private final ReviewRepository reviewRepository;

    public long countCompletedGatherings(Long userId) {
        List<Long> activeGatheringIds = gatheringMemberRepository.findActiveGatheringIdsByUserId(userId);
        if (activeGatheringIds.isEmpty()) {
            return 0L;
        }
        return gatheringRepository.findByIdIn(activeGatheringIds).stream()
                .filter(g -> g.getStatus() == GatheringStatus.COMPLETED)
                .count();
    }

    public BigDecimal calculateAvgAchievementRate(Long userId) {
        List<Long> activeGatheringIds = gatheringMemberRepository.findActiveGatheringIdsByUserId(userId);
        if (activeGatheringIds.isEmpty()) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        List<Long> completedGatheringIds = gatheringRepository.findByIdIn(activeGatheringIds).stream()
                .filter(g -> g.getStatus() == GatheringStatus.COMPLETED)
                .map(Gathering::getId)
                .toList();

        if (completedGatheringIds.isEmpty()) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal totalRate = BigDecimal.ZERO;
        int countWithTodos = 0;

        for (Long gatheringId : completedGatheringIds) {
            long total = todoRepository.countByGatheringIdAndUserId(gatheringId, userId);
            if (total == 0) {
                continue;
            }
            long completed = todoRepository.countByGatheringIdAndUserIdAndIsCompletedTrue(gatheringId, userId);
            BigDecimal rate = BigDecimal.valueOf(completed * 100L)
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
            totalRate = totalRate.add(rate);
            countWithTodos++;
        }

        if (countWithTodos == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        return totalRate.divide(BigDecimal.valueOf(countWithTodos), 1, RoundingMode.HALF_UP);
    }

    public long countReviews(Long userId) {
        return reviewRepository.countByTargetUserId(userId);
    }
}
