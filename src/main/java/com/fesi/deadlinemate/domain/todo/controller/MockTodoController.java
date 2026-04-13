package com.fesi.deadlinemate.domain.todo.controller;

import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.CreateTodoRequest;
import com.fesi.deadlinemate.domain.todo.dto.MockTodoDtos.UpdateTodoRequest;
import com.fesi.deadlinemate.domain.todo.service.MockTodoService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/mock")
public class MockTodoController {
    private final MockTodoService service;

    public MockTodoController(MockTodoService service) {
        this.service = service;
    }

    @GetMapping("/gatherings/{gatheringId}/todos")
    public ResponseEntity<ApiResponse<?>> getTodos(
            @PathVariable Long gatheringId,
            @RequestParam(required = false) Integer week
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getTodos(gatheringId, week)));
    }

    @GetMapping("/gatherings/{gatheringId}/todos/me")
    public ResponseEntity<ApiResponse<?>> getMyTodos(
            @PathVariable Long gatheringId,
            @RequestParam(required = false) Integer week
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getMyTodos(gatheringId, week)));
    }

    @PostMapping("/gatherings/{gatheringId}/todos")
    public ResponseEntity<ApiResponse<?>> createTodo(
            @PathVariable Long gatheringId,
            @RequestBody CreateTodoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createTodo(gatheringId, request), "Todo가 생성되었습니다."));
    }

    @PatchMapping("/gatherings/{gatheringId}/todos/{todoId}")
    public ResponseEntity<ApiResponse<?>> updateTodo(
            @PathVariable Long gatheringId,
            @PathVariable Long todoId,
            @RequestBody UpdateTodoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.updateTodo(gatheringId, todoId, request), "Todo가 수정되었습니다."));
    }

    @DeleteMapping("/gatherings/{gatheringId}/todos/{todoId}")
    public ResponseEntity<ApiResponse<?>> deleteTodo(
            @PathVariable Long gatheringId,
            @PathVariable Long todoId
    ) {
        service.deleteTodo(gatheringId, todoId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "Todo가 삭제되었습니다."));
    }
}
