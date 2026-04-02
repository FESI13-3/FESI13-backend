package com.fesi.deadlinemate.domain.category.dto;

import com.fesi.deadlinemate.domain.category.entity.Category;
import lombok.Builder;

@Builder
public record CategoryResponse(
        Long id,
        String name
) {
    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
