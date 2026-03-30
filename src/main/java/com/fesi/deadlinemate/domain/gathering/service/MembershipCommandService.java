package com.fesi.deadlinemate.domain.gathering.service;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringMemberRepository;
import com.fesi.deadlinemate.domain.gathering.repository.GatheringRepository;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipCommandService {

    private final GatheringMemberRepository gatheringMemberRepository;
    private final GatheringRepository gatheringRepository;

    public void kickMember(Long gatheringId, Long targetUserId, Long requesterId) {
        GatheringMember requester = findActiveMember(gatheringId, requesterId);
        if (!requester.isLeader()) {
            throw new BusinessException(ErrorCode.INVALID_GATHERING_LEADER);
        }
        if (targetUserId.equals(requesterId)) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_LEADER);
        }

        GatheringMember target = findActiveMember(gatheringId, targetUserId);
        target.deactivate();
        decreaseCurrentMembers(gatheringId);
    }

    public void leaveGathering(Long gatheringId, Long userId) {
        GatheringMember member = findActiveMember(gatheringId, userId);
        if (member.isLeader()) {
            throw new BusinessException(ErrorCode.LEADER_CANNOT_LEAVE);
        }
        member.deactivate();
        decreaseCurrentMembers(gatheringId);
    }

    private GatheringMember findActiveMember(Long gatheringId, Long userId) {
        return gatheringMemberRepository.findByGatheringIdAndUserId(gatheringId, userId)
                .filter(GatheringMember::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void decreaseCurrentMembers(Long gatheringId) {
        gatheringRepository.decreaseCurrentMembers(gatheringId);
    }
}
