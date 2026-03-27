package com.fesi.deadlinemate.global.mock.support;

import com.fesi.deadlinemate.domain.application.dto.MockApplicationEntity;
import com.fesi.deadlinemate.domain.gathering.entity.MockGatheringEntity;
import com.fesi.deadlinemate.domain.todo.entity.MockTodoEntity;
import com.fesi.deadlinemate.global.mock.MockStore;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;

@Component
public class MockFinder {
    private final MockStore store;

    public MockFinder(MockStore store) {
        this.store = store;
    }

    public MockGatheringEntity getGathering(Long gatheringId) {
        MockGatheringEntity gathering = store.gatherings.get(gatheringId);
        if (gathering == null) {
            throw new NoSuchElementException("모임을 찾을 수 없습니다.");
        }
        return gathering;
    }

    public MockApplicationEntity getApplication(Long applicationId) {
        MockApplicationEntity application = store.applications.get(applicationId);
        if (application == null) {
            throw new NoSuchElementException("신청 정보를 찾을 수 없습니다.");
        }
        return application;
    }

    public MockTodoEntity getTodo(Long todoId) {
        MockTodoEntity todo = store.todos.get(todoId);
        if (todo == null) {
            throw new NoSuchElementException("Todo를 찾을 수 없습니다.");
        }
        return todo;
    }
}
