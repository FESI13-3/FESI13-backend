package com.fesi.deadlinemate.global.config;

import com.fesi.deadlinemate.domain.category.entity.Category;
import com.fesi.deadlinemate.domain.category.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "개발", "어학", "독서", "자격증", "디자인"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : DEFAULT_CATEGORIES) {
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(Category.builder().name(name).build());
            }
        }
    }
}
