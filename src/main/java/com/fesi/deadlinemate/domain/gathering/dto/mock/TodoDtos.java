package com.fesi.deadlinemate.domain.gathering.dto.mock;

import java.time.OffsetDateTime;
import java.util.List;

public class TodoDtos {
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

    public static class TodoEntity {
        public Long id;
        public Long gatheringId;
        public Long userId;
        public Integer week;
        public String content;
        public Boolean isCompleted;
        public OffsetDateTime createdAt;
    }
}
