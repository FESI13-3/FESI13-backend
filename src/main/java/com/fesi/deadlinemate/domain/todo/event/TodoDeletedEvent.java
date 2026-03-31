package com.fesi.deadlinemate.domain.todo.event;

public record TodoDeletedEvent(
        Long todoId,
        Long gatheringId,
        Long userId,
        int weekNumber
) {
}
