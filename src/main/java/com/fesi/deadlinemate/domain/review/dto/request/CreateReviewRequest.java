package com.fesi.deadlinemate.domain.review.dto.request;

import com.fesi.deadlinemate.domain.review.command.CreateReviewCommand;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateReviewRequest(
        @Valid @NotEmpty List<ReviewItem> reviews
) {
    public record ReviewItem(
            @Schema(example = "2")
            @NotNull Long targetUserId,

            @ArraySchema(schema = @Schema(example = "시간약속을 잘 지켜요"))
            @NotEmpty List<String> tags,

            @Schema(example = "최고의 팀원")
            String matesTag,

            @Schema(example = "함께 공부하기 정말 좋은 분이에요. 적극적으로 참여해주셨습니다.")
            String comment
    ) {}

    public CreateReviewCommand toCommand(Long gatheringId, Long reviewerId) {
        return new CreateReviewCommand(
                gatheringId,
                reviewerId,
                reviews.stream()
                        .map(item -> new CreateReviewCommand.ReviewItem(
                                item.targetUserId(), item.tags(), item.matesTag(), item.comment()))
                        .toList()
        );
    }
}
