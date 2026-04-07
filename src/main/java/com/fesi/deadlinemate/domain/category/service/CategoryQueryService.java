package com.fesi.deadlinemate.domain.category.service;

import com.fesi.deadlinemate.domain.category.dto.CategoryListResponse;
import com.fesi.deadlinemate.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    public CategoryListResponse getCategories() {
        return CategoryListResponse.from(categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
    }
}
