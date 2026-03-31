package com.fesi.deadlinemate.domain.gathering.entity;

public enum GatheringStatus {
    RECRUITING, IN_PROGRESS, COMPLETED, CANCELLED;

    public static GatheringStatus fromString(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        return switch (status.toLowerCase()) {
            case "recruiting" -> RECRUITING;
            case "in_progress" -> IN_PROGRESS;
            case "completed" -> COMPLETED;
            default -> null;
        };
    }
}
