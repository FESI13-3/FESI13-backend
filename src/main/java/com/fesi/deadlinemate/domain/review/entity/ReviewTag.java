package com.fesi.deadlinemate.domain.review.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewTag {

    DILIGENT("성실해요"),
    GOOD_COMMUNICATION("소통이 좋아요"),
    HELPFUL("도움이 돼요"),
    PUNCTUAL("시간 약속을 잘 지켜요"),
    WANT_AGAIN("다시 만나고 싶어요");

    private final String displayName;

    public static ReviewTag fromDisplayName(String displayName) {
        for (ReviewTag tag : values()) {
            if (tag.displayName.equals(displayName)) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }
}
