package com.fesi.deadlinemate.domain.todo.dto.response;

import com.fesi.deadlinemate.domain.todo.entity.Todo;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CreateTodoResponse(
        Long id,
        int week,
        String content,
        boolean isCompleted,
        LocalDateTime createdAt
) {
    public static CreateTodoResponse from(Todo todo) {
        return CreateTodoResponse.builder()
                .id(todo.getId())
                .week(todo.getWeekNumber())
                .content(todo.getContent())
                .isCompleted(todo.isCompleted())
                .createdAt(todo.getCreatedAt())
                .build();
    }
}
