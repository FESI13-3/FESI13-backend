package com.fesi.deadlinemate.domain.todo.command;

import lombok.Builder;

@Builder
public record UpdateTodoCommand(
        Long gatheringId,
        Long todoId,
        Long userId,
        String content,
        Boolean isCompleted
) {
}
