package com.fesi.deadlinemate.domain.like.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.like.entity.GatheringLike;
import com.fesi.deadlinemate.domain.like.event.GatheringLikedEvent;
import com.fesi.deadlinemate.domain.like.event.GatheringUnlikedEvent;
import com.fesi.deadlinemate.domain.like.repository.GatheringLikeRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class GatheringLikeServiceTest {

    @Mock
    private GatheringRepository gatheringRepository;

    @Mock
    private GatheringLikeRepository gatheringLikeRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GatheringLikeService gatheringLikeService;

    @Test
    @DisplayName("찜한 모임 ID 목록을 조회한다")
    void getLikedGatheringIds_success() {
        // given
        Long userId = 100L;
        List<Long> expectedIds = List.of(1L, 3L, 7L);
        when(gatheringLikeRepository.findGatheringIdsByUserId(userId)).thenReturn(expectedIds);

        // when
        List<Long> result = gatheringLikeService.getLikedGatheringIds(userId);

        // then
        assertThat(result).containsExactly(1L, 3L, 7L);
        verify(gatheringLikeRepository).findGatheringIdsByUserId(userId);
    }

    @Test
    @DisplayName("찜한 모임이 없으면 빈 목록을 반환한다")
    void getLikedGatheringIds_empty() {
        // given
        Long userId = 100L;
        when(gatheringLikeRepository.findGatheringIdsByUserId(userId)).thenReturn(List.of());

        // when
        List<Long> result = gatheringLikeService.getLikedGatheringIds(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("모임을 찜하면 찜 정보를 저장하고 이벤트를 발행한다")
    void like_success() {
        // given
        Long gatheringId = 1L;
        Long userId = 100L;

        when(userClient.existsById(userId)).thenReturn(true);
        when(gatheringRepository.existsById(gatheringId)).thenReturn(true);

        // when
        gatheringLikeService.like(gatheringId, userId);

        // then
        ArgumentCaptor<GatheringLike> likeCaptor = ArgumentCaptor.forClass(GatheringLike.class);
        verify(gatheringLikeRepository).saveAndFlush(likeCaptor.capture());

        GatheringLike saved = likeCaptor.getValue();
        assertThat(saved.getGatheringId()).isEqualTo(gatheringId);
        assertThat(saved.getUserId()).isEqualTo(userId);

        ArgumentCaptor<GatheringLikedEvent> eventCaptor = ArgumentCaptor.forClass(GatheringLikedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        GatheringLikedEvent event = eventCaptor.getValue();
        assertThat(event.gatheringId()).isEqualTo(gatheringId);
        assertThat(event.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("이미 찜한 모임이면 예외가 발생한다")
    void like_fail_whenAlreadyLiked() {
        // given
        Long gatheringId = 1L;
        Long userId = 100L;

        when(userClient.existsById(userId)).thenReturn(true);
        when(gatheringRepository.existsById(gatheringId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(gatheringLikeRepository)
                .saveAndFlush(any(GatheringLike.class));

        // when & then
        assertThatThrownBy(() -> gatheringLikeService.like(gatheringId, userId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ALREADY_GATHERING_LIKED);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("찜한 모임을 취소하면 찜 정보를 삭제하고 이벤트를 발행한다")
    void unlike_success() {
        // given
        Long gatheringId = 1L;
        Long userId = 100L;

        GatheringLike gatheringLike = GatheringLike.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .build();

        when(userClient.existsById(userId)).thenReturn(true);
        when(gatheringRepository.existsById(gatheringId)).thenReturn(true);
        when(gatheringLikeRepository.findByGatheringIdAndUserId(gatheringId, userId))
                .thenReturn(Optional.of(gatheringLike));

        // when
        gatheringLikeService.unlike(gatheringId, userId);

        // then
        verify(gatheringLikeRepository).delete(gatheringLike);

        ArgumentCaptor<GatheringUnlikedEvent> eventCaptor = ArgumentCaptor.forClass(GatheringUnlikedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        GatheringUnlikedEvent event = eventCaptor.getValue();
        assertThat(event.gatheringId()).isEqualTo(gatheringId);
        assertThat(event.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("찜한 이력이 없으면 찜 취소 시 예외가 발생한다")
    void unlike_fail_whenLikeNotFound() {
        // given
        Long gatheringId = 1L;
        Long userId = 100L;

        when(userClient.existsById(userId)).thenReturn(true);
        when(gatheringRepository.existsById(gatheringId)).thenReturn(true);
        when(gatheringLikeRepository.findByGatheringIdAndUserId(gatheringId, userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gatheringLikeService.unlike(gatheringId, userId))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.GATHERING_LIKE_NOT_FOUND);

        verify(gatheringLikeRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}