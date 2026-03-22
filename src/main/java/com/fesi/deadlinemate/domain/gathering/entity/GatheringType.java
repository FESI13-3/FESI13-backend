package com.fesi.deadlinemate.domain.gathering.entity;

import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;

public enum GatheringType {
    STUDY("스터디"),
    PROJECT("프로젝트");

    private final String displayName;

    GatheringType(String displayName) {
        this.displayName = displayName;
    }

    public static GatheringType from(String value) {
        return switch (value) {
            case "스터디", "STUDY" -> STUDY;
            case "프로젝트", "PROJECT" -> PROJECT;
            default -> throw new BusinessException(ErrorCode.INVALID_GATHERING_TYPE);
        };
    }

    public String getDisplayName() {
        return displayName;
    }
}
