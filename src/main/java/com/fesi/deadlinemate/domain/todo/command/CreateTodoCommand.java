package com.fesi.deadlinemate.domain.todo.command;

import lombok.Builder;

@Builder
public record CreateTodoCommand(
        Long gatheringId,
        Long userId,
        int week,
        String content
) {
}
