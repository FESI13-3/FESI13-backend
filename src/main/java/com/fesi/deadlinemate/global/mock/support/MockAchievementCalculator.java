package com.fesi.deadlinemate.global.mock.support;

import com.fesi.deadlinemate.domain.todo.dto.MockTodoEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockAchievementCalculator {
    public double calcRate(List<MockTodoEntity> todos) {
        if (todos == null || todos.isEmpty()) {
            return 0.0;
        }
        long completed = todos.stream()
                .filter(t -> Boolean.TRUE.equals(t.isCompleted))
                .count();
        return round((completed * 100.0) / todos.size());
    }

    public double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
