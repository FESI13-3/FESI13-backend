package com.fesi.deadlinemate.domain.gathering.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.fesi.deadlinemate.domain.gathering.entity.Gathering;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MembershipCommandServiceTest {

    @Mock
    private GatheringMemberRepository gatheringMemberRepository;
    @Mock
    private GatheringRepository gatheringRepository;

    @InjectMocks
    private MembershipCommandService membershipCommandService;

    @Nested
    @DisplayName("멤버 퇴출")
    class KickMember {

        @Test
        @DisplayName("모임장이 멤버를 퇴출한다")
        void kickMember() {
            GatheringMember leader = member(1L, 1L, 10L, GatheringRole.LEADER);
            GatheringMember target = member(2L, 1L, 20L, GatheringRole.MEMBER);
            Gathering gathering = gathering(1L, 3);

            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 10L))
                    .willReturn(Optional.of(leader));
            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 20L))
                    .willReturn(Optional.of(target));
            given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));

            membershipCommandService.kickMember(1L, 20L, 10L);

            assertThat(target.isActive()).isFalse();
            assertThat(gathering.getCurrentMembers()).isEqualTo(2);
        }

        @Test
        @DisplayName("모임장이 아닌 멤버가 퇴출하면 예외가 발생한다")
        void notLeaderThrows() {
            GatheringMember nonLeader = member(1L, 1L, 10L, GatheringRole.MEMBER);
            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 10L))
                    .willReturn(Optional.of(nonLeader));

            assertThatThrownBy(() -> membershipCommandService.kickMember(1L, 20L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_GATHERING_LEADER);
        }

        @Test
        @DisplayName("자기 자신을 퇴출하면 예외가 발생한다")
        void cannotKickSelf() {
            GatheringMember leader = member(1L, 1L, 10L, GatheringRole.LEADER);
            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 10L))
                    .willReturn(Optional.of(leader));

            assertThatThrownBy(() -> membershipCommandService.kickMember(1L, 10L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CANNOT_KICK_LEADER);
        }
    }

    @Nested
    @DisplayName("모임 탈퇴")
    class LeaveGathering {

        @Test
        @DisplayName("일반 멤버가 모임을 탈퇴한다")
        void leaveGathering() {
            GatheringMember member = member(1L, 1L, 20L, GatheringRole.MEMBER);
            Gathering gathering = gathering(1L, 3);

            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 20L))
                    .willReturn(Optional.of(member));
            given(gatheringRepository.findById(1L)).willReturn(Optional.of(gathering));

            membershipCommandService.leaveGathering(1L, 20L);

            assertThat(member.isActive()).isFalse();
            assertThat(gathering.getCurrentMembers()).isEqualTo(2);
        }

        @Test
        @DisplayName("모임장이 탈퇴하면 예외가 발생한다")
        void leaderCannotLeave() {
            GatheringMember leader = member(1L, 1L, 10L, GatheringRole.LEADER);
            given(gatheringMemberRepository.findByGatheringIdAndUserId(1L, 10L))
                    .willReturn(Optional.of(leader));

            assertThatThrownBy(() -> membershipCommandService.leaveGathering(1L, 10L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.LEADER_CANNOT_LEAVE);
        }
    }

    private GatheringMember member(Long id, Long gatheringId, Long userId, GatheringRole role) {
        GatheringMember m = GatheringMember.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .role(role)
                .overallAchievementRate(BigDecimal.ZERO)
                .isActive(true)
                .build();
        setField(m, "id", id);
        return m;
    }

    private Gathering gathering(Long id, int currentMembers) {
        Gathering g = Gathering.builder()
                .leaderId(10L)
                .type(GatheringType.STUDY)
                .category("test").title("test").shortDescription("s").description("d").goal("g")
                .maxMembers(5).currentMembers(currentMembers)
                .recruitDeadline(LocalDate.now()).startDate(LocalDate.now()).endDate(LocalDate.now().plusWeeks(4))
                .totalWeeks(4).status(GatheringStatus.IN_PROGRESS).viewCount(0)
                .build();
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
