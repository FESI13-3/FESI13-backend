package com.fesi.deadlinemate.global.mock.support;

import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.TodoItemDto;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoEntity;
import org.springframework.stereotype.Component;

@Component
public class MockTodoMapper {
    public TodoItemDto toTodoItem(MockTodoEntity todo) {
        return new TodoItemDto(
                todo.id,
                todo.userId,
                "유저" + todo.userId,
                todo.week,
                todo.content,
                todo.isCompleted,
                todo.createdAt
        );
    }
}
