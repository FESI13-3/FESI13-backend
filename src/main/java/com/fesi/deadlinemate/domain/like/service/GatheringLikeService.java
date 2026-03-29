package com.fesi.deadlinemate.domain.like.service;

import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.like.entity.GatheringLike;
import com.fesi.deadlinemate.domain.like.event.GatheringLikedEvent;
import com.fesi.deadlinemate.domain.like.event.GatheringUnlikedEvent;
import com.fesi.deadlinemate.domain.like.repository.GatheringLikeRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatheringLikeService {

    private final GatheringRepository gatheringRepository;
    private final GatheringLikeRepository gatheringLikeRepository;
    private final UserClient userClient;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void like(Long gatheringId, Long userId) {
        validateUserExists(userId);
        validateGatheringExists(gatheringId);

        GatheringLike gatheringLike = GatheringLike.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .build();

        try {
            gatheringLikeRepository.saveAndFlush(gatheringLike);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_GATHERING_LIKED);
        }

        eventPublisher.publishEvent(new GatheringLikedEvent(gatheringId, userId));
    }

    @Transactional
    public void unlike(Long gatheringId, Long userId) {
        validateUserExists(userId);
        validateGatheringExists(gatheringId);

        GatheringLike gatheringLike = gatheringLikeRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_LIKE_NOT_FOUND));

        gatheringLikeRepository.delete(gatheringLike);

        eventPublisher.publishEvent(new GatheringUnlikedEvent(gatheringId, userId));
    }

    private void validateUserExists(Long userId) {
        if (userId == null || !userClient.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateGatheringExists(Long gatheringId) {
        boolean exists = gatheringRepository.existsById(gatheringId);
        if (!exists) {
            throw new BusinessException(ErrorCode.GATHERING_NOT_FOUND);
        }
    }
}
