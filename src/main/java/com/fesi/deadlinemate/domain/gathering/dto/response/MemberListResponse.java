package com.fesi.deadlinemate.domain.gathering.dto.response;

import com.fesi.deadlinemate.domain.gathering.entity.GatheringMember;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.user.client.dto.UserInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder
public record MemberListResponse(List<MemberItem> members) {

    @Builder
    public record MemberItem(
            Long userId,
            String nickname,
            String profileImage,
            GatheringRole role,
            BigDecimal overallAchievementRate,
            boolean isActive
    ) {
    }

    public static MemberListResponse of(List<GatheringMember> members, Map<Long, UserInfo> userMap) {
        List<MemberItem> items = members.stream()
                .map(member -> {
                    UserInfo user = userMap.get(member.getUserId());
                    return MemberItem.builder()
                            .userId(member.getUserId())
                            .nickname(user != null ? user.getNickname() : null)
                            .profileImage(user != null ? user.getProfileImage() : null)
                            .role(member.getRole())
                            .overallAchievementRate(member.getOverallAchievementRate())
                            .isActive(member.isActive())
                            .build();
                })
                .toList();
        return MemberListResponse.builder().members(items).build();
    }
}
