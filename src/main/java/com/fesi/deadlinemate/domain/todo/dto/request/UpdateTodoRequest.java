package com.fesi.deadlinemate.domain.todo.dto.request;

import com.fesi.deadlinemate.domain.todo.command.UpdateTodoCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateTodoRequest(
        @Size(max = 400, message = "할 일 내용은 400자 이하여야 합니다.")
        String content,

        Boolean isCompleted
) {
    public UpdateTodoCommand toCommand(Long gatheringId, Long todoId, Long userId) {
        return UpdateTodoCommand.builder()
                .gatheringId(gatheringId)
                .todoId(todoId)
                .userId(userId)
                .content(content)
                .isCompleted(isCompleted)
                .build();
    }

    @AssertTrue(message = "변경할 값이 하나 이상 필요합니다.")
    public boolean hasAnyUpdatableField() {
        return content != null || isCompleted != null;
    }
}
