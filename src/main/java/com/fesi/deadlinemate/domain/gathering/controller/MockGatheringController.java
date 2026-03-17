package com.fesi.deadlinemate.domain.gathering.controller;

import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.CreateApplicationRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.ApplicationDtos.UpdateApplicationStatusRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.GatheringDtos.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.CreateTodoRequest;
import com.fesi.deadlinemate.domain.gathering.dto.mock.TodoDtos.UpdateTodoRequest;
import com.fesi.deadlinemate.domain.gathering.service.MockGatheringService;
import com.fesi.deadlinemate.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MockGatheringController {

    private final MockGatheringService service;

    public MockGatheringController(MockGatheringService service) {
        this.service = service;
    }

    @GetMapping("/gatherings")
    public ResponseEntity<ApiResponse<?>> getGatherings(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "recruiting") String status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                service.getGatherings(type, category, sort, status, query, page, limit)
        ));
    }

    @PostMapping("/gatherings")
    public ResponseEntity<ApiResponse<?>> createGathering(@RequestBody CreateGatheringRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        java.util.Map.of("gathering", service.createGathering(request)),
                        "모임이 생성되었습니다."
                ));
    }

    @GetMapping("/gatherings/main")
    public ResponseEntity<ApiResponse<?>> getMain(@RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success(service.getMain(limit)));
    }

    @GetMapping("/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<?>> getGatheringDetail(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getGatheringDetail(gatheringId)));
    }

    @PutMapping("/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<?>> updateGathering(
            @PathVariable Long gatheringId,
            @RequestBody UpdateGatheringRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                java.util.Map.of("gathering", service.updateGathering(gatheringId, request)),
                "모임이 수정되었습니다."
        ));
    }

    @DeleteMapping("/gatherings/{gatheringId}")
    public ResponseEntity<ApiResponse<?>> deleteGathering(@PathVariable Long gatheringId) {
        service.deleteGathering(gatheringId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "모임이 삭제되었습니다."));
    }

    @PostMapping("/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<?>> createApplication(
            @PathVariable Long gatheringId,
            @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createApplication(gatheringId, request), "신청이 완료되었습니다."));
    }

    @GetMapping("/gatherings/{gatheringId}/applications")
    public ResponseEntity<ApiResponse<?>> getApplications(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getApplications(gatheringId)));
    }

    @PatchMapping("/gatherings/{gatheringId}/applications/{applicationId}")
    public ResponseEntity<ApiResponse<?>> updateApplicationStatus(
            @PathVariable Long gatheringId,
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updateApplicationStatus(gatheringId, applicationId, request),
                "신청 상태가 변경되었습니다."
        ));
    }

    @DeleteMapping("/gatherings/{gatheringId}/applications/{applicationId}")
    public ResponseEntity<ApiResponse<?>> cancelApplication(
            @PathVariable Long gatheringId,
            @PathVariable Long applicationId
    ) {
        service.cancelApplication(gatheringId, applicationId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "신청이 취소되었습니다."));
    }

    @GetMapping("/users/me/applications")
    public ResponseEntity<ApiResponse<?>> getMyApplications() {
        return ResponseEntity.ok(ApiResponse.success(service.getMyApplications()));
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

    @GetMapping("/gatherings/{gatheringId}/achievements")
    public ResponseEntity<ApiResponse<?>> getAchievements(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAchievements(gatheringId)));
    }

    @GetMapping("/gatherings/{gatheringId}/achievements/ranking")
    public ResponseEntity<ApiResponse<?>> getRanking(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getAchievementRanking(gatheringId)));
    }

    @GetMapping("/gatherings/{gatheringId}/report")
    public ResponseEntity<ApiResponse<?>> getReport(@PathVariable Long gatheringId) {
        return ResponseEntity.ok(ApiResponse.success(service.getReport(gatheringId)));
    }

    @PostMapping("/gatherings/{gatheringId}/likes")
    public ResponseEntity<ApiResponse<?>> like(@PathVariable Long gatheringId) {
        service.like(gatheringId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(java.util.Map.of("success", true), "찜하기가 완료되었습니다."));
    }

    @DeleteMapping("/gatherings/{gatheringId}/likes")
    public ResponseEntity<ApiResponse<?>> unlike(@PathVariable Long gatheringId) {
        service.unlike(gatheringId);
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of("success", true), "찜하기가 취소되었습니다."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleConflict(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<?>> handleForbidden(SecurityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(java.util.NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    }
}
