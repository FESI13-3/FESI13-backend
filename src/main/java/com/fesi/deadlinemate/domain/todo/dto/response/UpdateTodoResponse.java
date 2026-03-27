package com.fesi.deadlinemate.domain.todo.dto.response;

import com.fesi.deadlinemate.domain.todo.entity.Todo;
import lombok.Builder;

@Builder
public record UpdateTodoResponse(
        Long id,
        String content,
        boolean isCompleted
) {
    public static UpdateTodoResponse from(Todo todo) {
        return UpdateTodoResponse.builder()
                .id(todo.getId())
                .content(todo.getContent())
                .isCompleted(todo.isCompleted())
                .build();
    }
}
