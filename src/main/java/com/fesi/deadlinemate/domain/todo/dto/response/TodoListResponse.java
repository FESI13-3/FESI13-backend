package com.fesi.deadlinemate.domain.todo.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record TodoListResponse(
        List<TodoItemResponse> todos
) {
    public static TodoListResponse of(List<TodoItemResponse> todos) {
        return TodoListResponse.builder()
                .todos(todos)
                .build();
    }

    @Builder
    public record TodoItemResponse(
            Long id,
            Long userId,
            String nickname,
            int week,
            String content,
            boolean isCompleted,
            LocalDateTime createdAt
    ) {
    }
}
