package com.fesi.deadlinemate.domain.category.repository;

import com.fesi.deadlinemate.domain.category.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIdIn(List<Long> ids);
}
