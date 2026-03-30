package com.fesi.deadlinemate.domain.todo.controller;

import com.fesi.deadlinemate.domain.todo.dto.request.CreateTodoRequest;
import com.fesi.deadlinemate.domain.todo.dto.request.UpdateTodoRequest;
import com.fesi.deadlinemate.domain.todo.dto.response.CreateTodoResponse;
import com.fesi.deadlinemate.domain.todo.dto.response.MyTodoListResponse;
import com.fesi.deadlinemate.domain.todo.dto.response.TodoListResponse;
import com.fesi.deadlinemate.domain.todo.dto.response.UpdateTodoResponse;
import com.fesi.deadlinemate.domain.todo.service.TodoService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping("/{gatheringId}/todos")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateTodoResponse> createTodo(
            @PathVariable Long gatheringId,
            @RequestBody @Valid CreateTodoRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        CreateTodoResponse response = todoService.create(
                request.toCommand(gatheringId, userId)
        );

        return ApiResponse.success(response);
    }

    @PatchMapping("/{gatheringId}/todos/{todoId}")
    public ApiResponse<UpdateTodoResponse> updateTodo(
            @PathVariable Long gatheringId,
            @PathVariable Long todoId,
            @RequestBody @Valid UpdateTodoRequest request,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        UpdateTodoResponse response = todoService.update(
                request.toCommand(gatheringId, todoId, userId)
        );

        return ApiResponse.success(response);
    }

    @GetMapping("/{gatheringId}/todos")
    public ApiResponse<TodoListResponse> getTodos(
            @PathVariable Long gatheringId,
            @RequestParam(required = false) Integer week,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(todoService.getTodos(gatheringId, userId, week));
    }

    @GetMapping("/{gatheringId}/todos/me")
    public ApiResponse<MyTodoListResponse> getMyTodos(
            @PathVariable Long gatheringId,
            @RequestParam(required = false) Integer week,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(todoService.getMyTodos(gatheringId, userId, week));
    }

    @DeleteMapping("/{gatheringId}/todos/{todoId}")
    public ApiResponse<Void> deleteTodo(
            @PathVariable Long gatheringId,
            @PathVariable Long todoId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        todoService.delete(gatheringId, todoId, userId);
        return ApiResponse.success();
    }
}
