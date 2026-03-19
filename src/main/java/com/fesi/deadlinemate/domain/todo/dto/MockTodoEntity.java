package com.fesi.deadlinemate.domain.todo.dto;

import java.time.OffsetDateTime;

public class MockTodoEntity {
    public Long id;
    public Long gatheringId;
    public Long userId;
    public Integer week;
    public String content;
    public Boolean isCompleted;
    public OffsetDateTime createdAt;
}
