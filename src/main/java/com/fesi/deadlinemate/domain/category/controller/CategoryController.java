package com.fesi.deadlinemate.domain.category.controller;

import com.fesi.deadlinemate.domain.category.service.CategoryQueryService;
import com.fesi.deadlinemate.domain.category.dto.CategoryListResponse;
import com.fesi.deadlinemate.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gatherings")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryQueryService categoryQueryService;

    @GetMapping("/categories")
    public ApiResponse<CategoryListResponse> getCategories() {
        return ApiResponse.success(categoryQueryService.getCategories());
    }
}
