package com.fesi.deadlinemate.domain.todo.service;

import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.CreateTodoRequest;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.MyTodoResponse;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.TodoItemDto;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.TodoListResponse;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.UpdateTodoRequest;
import com.fesi.deadlinemate.domain.todo.entity.MockTodoEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import com.fesi.deadlinemate.global.mock.support.MockAchievementCalculator;
import com.fesi.deadlinemate.global.mock.support.MockAuthContext;
import com.fesi.deadlinemate.global.mock.support.MockFinder;
import com.fesi.deadlinemate.global.mock.support.MockPermissionService;
import com.fesi.deadlinemate.global.mock.support.MockTodoMapper;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MockTodoService {
    private final MockStore store;
    private final MockFinder finder;
    private final MockAuthContext authContext;
    private final MockPermissionService permissionService;
    private final MockTodoMapper todoMapper;
    private final MockAchievementCalculator calculator;

    public MockTodoService(
            MockStore store,
            MockFinder finder,
            MockAuthContext authContext,
            MockPermissionService permissionService,
            MockTodoMapper todoMapper,
            MockAchievementCalculator calculator
    ) {
        this.store = store;
        this.finder = finder;
        this.authContext = authContext;
        this.permissionService = permissionService;
        this.todoMapper = todoMapper;
        this.calculator = calculator;
    }

    public TodoListResponse getTodos(Long gatheringId, Integer week) {
        permissionService.validateMember(gatheringId);

        List<TodoItemDto> items = store.todos.values().stream()
                .filter(t -> t.gatheringId.equals(gatheringId))
                .filter(t -> week == null || t.week.equals(week))
                .sorted(Comparator.comparing((MockTodoEntity t) -> t.createdAt))
                .map(todoMapper::toTodoItem)
                .toList();

        return new TodoListResponse(items);
    }

    public MyTodoResponse getMyTodos(Long gatheringId, Integer week) {
        permissionService.validateMember(gatheringId);

        List<MockTodoEntity> myTodos = store.todos.values().stream()
                .filter(t -> t.gatheringId.equals(gatheringId))
                .filter(t -> t.userId.equals(authContext.currentUserId()))
                .filter(t -> week == null || t.week.equals(week))
                .toList();

        List<TodoItemDto> items = myTodos.stream()
                .map(todoMapper::toTodoItem)
                .toList();

        double weeklyRate = calculator.calcRate(myTodos);

        double overallRate = calculator.calcRate(
                store.todos.values().stream()
                        .filter(t -> t.gatheringId.equals(gatheringId))
                        .filter(t -> t.userId.equals(authContext.currentUserId()))
                        .toList()
        );

        return new MyTodoResponse(items, weeklyRate, overallRate);
    }

    public Map<String, Object> createTodo(Long gatheringId, CreateTodoRequest req) {
        permissionService.validateMember(gatheringId);

        MockTodoEntity todo = new MockTodoEntity();
        todo.id = store.todoSeq.getAndIncrement();
        todo.gatheringId = gatheringId;
        todo.userId = authContext.currentUserId();
        todo.week = req.week();
        todo.content = req.content();
        todo.isCompleted = false;
        todo.createdAt = OffsetDateTime.now();

        store.todos.put(todo.id, todo);

        return Map.of("todo", todoMapper.toTodoItem(todo));
    }

    public Map<String, Object> updateTodo(Long gatheringId, Long todoId, UpdateTodoRequest req) {
        MockTodoEntity todo = finder.getTodo(todoId);

        if (!todo.gatheringId.equals(gatheringId)
                || !todo.userId.equals(authContext.currentUserId())) {
            throw new IllegalArgumentException("본인 Todo만 수정할 수 있습니다.");
        }

        if (req.content() != null) {
            todo.content = req.content();
        }
        if (req.isCompleted() != null) {
            todo.isCompleted = req.isCompleted();
        }

        return Map.of(
                "todo", Map.of(
                        "id", todo.id,
                        "content", todo.content,
                        "isCompleted", todo.isCompleted
                )
        );
    }

    public void deleteTodo(Long gatheringId, Long todoId) {
        MockTodoEntity todo = finder.getTodo(todoId);

        if (!todo.gatheringId.equals(gatheringId)
                || !todo.userId.equals(authContext.currentUserId())) {
            throw new IllegalArgumentException("본인 Todo만 삭제할 수 있습니다.");
        }

        store.todos.remove(todoId);
    }
}
