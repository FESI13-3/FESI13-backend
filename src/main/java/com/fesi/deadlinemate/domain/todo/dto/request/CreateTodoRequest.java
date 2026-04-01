package com.fesi.deadlinemate.domain.todo.dto.request;

import com.fesi.deadlinemate.domain.todo.command.CreateTodoCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateTodoRequest(
        @NotNull(message = "주차는 필수입니다.")
        @Min(value = 1, message = "주차는 1 이상이어야 합니다.")
        Integer week,

        @NotBlank(message = "할 일 내용은 필수입니다.")
        @Size(max = 400, message = "할 일 내용은 400자 이하여야 합니다.")
        String content
) {
    public CreateTodoCommand toCommand(Long gatheringId, Long userId) {
        return CreateTodoCommand.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .week(week)
                .content(content)
                .build();
    }
}
