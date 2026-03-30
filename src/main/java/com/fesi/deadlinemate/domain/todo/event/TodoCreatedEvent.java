package com.fesi.deadlinemate.domain.todo.event;

public record TodoCreatedEvent(
        Long todoId,
        Long gatheringId,
        Long userId,
        int weekNumber,
        String content
) {
}
