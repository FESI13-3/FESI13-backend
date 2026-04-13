package com.fesi.deadlinemate.global.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.fesi.deadlinemate.domain.category.entity.Category;
import com.fesi.deadlinemate.domain.category.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryDataInitializerTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryDataInitializer initializer;

    @Test
    @DisplayName("카테고리가 하나도 없으면 5개를 모두 저장한다")
    void savesAllFiveCategoriesWhenNoneExist() throws Exception {
        given(categoryRepository.existsByName(anyString())).willReturn(false);

        initializer.run(null);

        then(categoryRepository).should(times(5)).save(any(Category.class));
    }

    @Test
    @DisplayName("저장되는 카테고리 이름은 개발, 어학, 독서, 자격증, 디자인이다")
    void savesExpectedCategoryNames() throws Exception {
        given(categoryRepository.existsByName(anyString())).willReturn(false);

        initializer.run(null);

        then(categoryRepository).should().save(argThat(c -> c.getName().equals("개발")));
        then(categoryRepository).should().save(argThat(c -> c.getName().equals("어학")));
        then(categoryRepository).should().save(argThat(c -> c.getName().equals("독서")));
        then(categoryRepository).should().save(argThat(c -> c.getName().equals("자격증")));
        then(categoryRepository).should().save(argThat(c -> c.getName().equals("디자인")));
    }

    @Test
    @DisplayName("이미 존재하는 카테고리는 저장하지 않는다")
    void skipsExistingCategory() throws Exception {
        given(categoryRepository.existsByName("개발")).willReturn(true);
        given(categoryRepository.existsByName(argThat(name -> !name.equals("개발")))).willReturn(false);

        initializer.run(null);

        then(categoryRepository).should(times(4)).save(any(Category.class));
        then(categoryRepository).should(never()).save(argThat(c -> c.getName().equals("개발")));
    }

    @Test
    @DisplayName("5개 카테고리가 모두 존재하면 아무것도 저장하지 않는다")
    void savesNothingWhenAllExist() throws Exception {
        given(categoryRepository.existsByName(anyString())).willReturn(true);

        initializer.run(null);

        then(categoryRepository).should(never()).save(any(Category.class));
    }

    @Test
    @DisplayName("멱등성 — 두 번 실행해도 이미 있는 카테고리는 중복 저장되지 않는다")
    void idempotent() throws Exception {
        // 첫 실행: 아무것도 없음 → 5개 저장
        given(categoryRepository.existsByName(anyString())).willReturn(false);
        initializer.run(null);

        // 두 번째 실행: 모두 존재 → 0개 저장
        given(categoryRepository.existsByName(anyString())).willReturn(true);
        initializer.run(null);

        // 총 5번만 save 호출
        then(categoryRepository).should(times(5)).save(any(Category.class));
    }
}
