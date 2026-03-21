package com.fesi.deadlinemate.domain.todo.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class MockTodoDtos {
    public record CreateTodoRequest(
            Integer week,
            String content
    ) {}

    public record UpdateTodoRequest(
            String content,
            Boolean isCompleted
    ) {}

    public record TodoItemDto(
            Long id,
            Long userId,
            String nickname,
            Integer week,
            String content,
            Boolean isCompleted,
            OffsetDateTime createdAt
    ) {}

    public record TodoListResponse(
            List<TodoItemDto> todos
    ) {}

    public record MyTodoResponse(
            List<TodoItemDto> todos,
            Double weeklyAchievementRate,
            Double overallAchievementRate
    ) {}
}
