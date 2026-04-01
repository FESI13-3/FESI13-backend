package com.fesi.deadlinemate.domain.todo.entity;

import com.fesi.deadlinemate.global.common.BaseTimeEntity;
import com.fesi.deadlinemate.global.error.BusinessException;
import com.fesi.deadlinemate.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todos", indexes = {
        @Index(name = "idx_todos_gathering_user_week", columnList = "gatheringId, userId, weekNumber")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long gatheringId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int weekNumber;

    @Column(nullable = false, length = 400)
    private String content;

    @Column(nullable = false)
    private boolean isCompleted;

    private LocalDateTime completedAt;

    @Builder
    public Todo(
            Long gatheringId,
            Long userId,
            int weekNumber,
            String content,
            boolean isCompleted,
            LocalDateTime completedAt
    ) {
        validateWeekNumber(weekNumber);
        validateContent(content);

        this.gatheringId = gatheringId;
        this.userId = userId;
        this.weekNumber = weekNumber;
        this.content = content;
        this.isCompleted = isCompleted;
        this.completedAt = completedAt;
    }

    public void validateOwner(Long requesterId) {
        validateOwnerOrThrow(requesterId, ErrorCode.TODO_UPDATE_FORBIDDEN);
    }

    public void validateDeletableBy(Long requesterId) {
        validateOwnerOrThrow(requesterId, ErrorCode.TODO_DELETE_FORBIDDEN);
    }

    public boolean updateContent(String content) {
        if (content == null) {
            return false;
        }

        String normalized = content.trim();
        validateContent(normalized);
        if (Objects.equals(this.content, normalized)) {
            return false;
        }
        this.content = normalized;
        return true;
    }

    public boolean changeCompleted(boolean completed) {
        if (this.isCompleted == completed) {
            return false;
        }

        this.isCompleted = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
        return true;
    }

    private void validateOwnerOrThrow(Long requesterId, ErrorCode errorCode) {
        if (!this.userId.equals(requesterId)) {
            throw new BusinessException(errorCode);
        }
    }

    private static void validateWeekNumber(int weekNumber) {
        if (weekNumber < 1) {
            throw new BusinessException(ErrorCode.INVALID_TODO_WEEK);
        }
    }

    private static void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_TODO_CONTENT);
        }
        if (content.length() > 400) {
            throw new BusinessException(ErrorCode.INVALID_TODO_CONTENT);
        }
    }
}
