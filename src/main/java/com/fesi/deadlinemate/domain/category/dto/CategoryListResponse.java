package com.fesi.deadlinemate.domain.category.dto;

import com.fesi.deadlinemate.domain.category.entity.Category;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryListResponse(
        List<CategoryResponse> categories
) {
    public static CategoryListResponse from(List<Category> categories) {
        return CategoryListResponse.builder()
                .categories(categories.stream()
                        .map(CategoryResponse::from)
                        .toList())
                .build();
    }
}
