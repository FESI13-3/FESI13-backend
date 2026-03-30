package com.fesi.deadlinemate.domain.gathering.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.fesi.deadlinemate.domain.gathering.dto.response.MemberListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.MyGatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringTagRepository;
import com.fesi.deadlinemate.domain.user.client.UserClient;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class MembershipQueryServiceTest {

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;
    @Mock
    private GatheringRepository gatheringRepository;
    @Mock
    private GatheringTagRepository gatheringTagRepository;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private MembershipQueryService membershipQueryService;

    @Nested
    @DisplayName("내 모임 목록 조회")
    class GetMyGatherings {

        @Test
        @DisplayName("참여 중인 모임이 없으면 빈 목록을 반환한다")
        void emptyGatherings() {
            given(gatheringMemberRepository.findActiveGatheringIdsByUserId(1L)).willReturn(List.of());

            MyGatheringListResponse response = membershipQueryService.getMyGatherings(1L, "all", 1, 12);

            assertThat(response.gatherings()).isEmpty();
            assertThat(response.totalCount()).isZero();
        }

        @Test
        @DisplayName("참여 중인 모임 목록을 반환한다")
        void getMyGatherings() {
            Gathering gathering = gathering(1L, 3);
            GatheringMember member = member(1L, 1L, 10L, GatheringRole.MEMBER);

            given(gatheringMemberRepository.findActiveGatheringIdsByUserId(10L)).willReturn(List.of(1L));
            given(gatheringRepository.findByIdInOrderByCreatedAtDesc(any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(gathering)));
            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 10L))
                    .willReturn(Optional.of(member));
            given(gatheringTagRepository.findByGatheringIdOrderByIdAsc(1L)).willReturn(List.of());

            MyGatheringListResponse response = membershipQueryService.getMyGatherings(10L, "all", 1, 12);

            assertThat(response.gatherings()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("멤버 목록 조회")
    class GetMembers {

        @Test
        @DisplayName("모임 멤버 목록을 반환한다")
        void getMembers() {
            GatheringMember member = member(1L, 1L, 10L, GatheringRole.LEADER);
            given(gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(1L, 10L)).willReturn(true);
            given(gatheringMemberRepository.findByGatheringIdAndIsActiveTrueOrderByIdAsc(1L))
                    .willReturn(List.of(member));
            given(userClient.findById(10L)).willReturn(new UserInfo(10L, "leader", null));

            MemberListResponse response = membershipQueryService.getMembers(1L, 10L);

            assertThat(response.members()).hasSize(1);
        }

        @Test
        @DisplayName("멤버가 아닌 사용자가 조회하면 예외가 발생한다")
        void notMemberThrows() {
            given(gatheringMemberRepository.existsByGatheringIdAndUserIdAndIsActiveTrue(1L, 99L)).willReturn(false);

            assertThatThrownBy(() -> membershipQueryService.getMembers(1L, 99L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_A_MEMBER);
        }
    }

    private GatheringMember member(Long id, Long gatheringId, Long userId, GatheringRole role) {
        GatheringMember m = GatheringMember.builder()
                .gatheringId(gatheringId).userId(userId).role(role)
                .overallAchievementRate(BigDecimal.ZERO).isActive(true).build();
        setField(m, "id", id);
        return m;
    }

    private Gathering gathering(Long id, int currentMembers) {
        Gathering g = Gathering.builder()
                .leaderId(10L).type(GatheringType.STUDY)
                .category("test").title("test").shortDescription("s").description("d").goal("g")
                .maxMembers(5).currentMembers(currentMembers)
                .recruitDeadline(LocalDate.now()).startDate(LocalDate.now()).endDate(LocalDate.now().plusWeeks(4))
                .totalWeeks(4).status(GatheringStatus.IN_PROGRESS).viewCount(0).build();
        setField(g, "id", id);
        return g;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
