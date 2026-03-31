package com.fesi.deadlinemate.domain.todo.event;

public record TodoUpdatedEvent(
        Long todoId,
        Long gatheringId,
        Long userId,
        String content,
        boolean isCompleted
) {
}
