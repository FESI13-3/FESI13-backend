package com.fesi.deadlinemate.domain.review.entity;

import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MatesTag {

    BEST_MATE("최고의 메이트에요"),
    GOOD_ENERGY("에너지가 넘쳐요"),
    MOTIVATING("동기부여가 돼요"),
    STEADY_RUNNER("꾸준히 달려요"),
    PACEMAKER("페이스메이커예요");

    private final String displayName;

    public static MatesTag fromDisplayName(String displayName) {
        for (MatesTag tag : values()) {
            if (tag.displayName.equals(displayName)) {
                return tag;
            }
        }
        throw new BusinessException(ErrorCode.INVALID_MATES_TAG);
    }
}
