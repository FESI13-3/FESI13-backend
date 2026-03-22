package com.fesi.deadlinemate.domain.gathering.event;

public interface GatheringEventPublisher {
    void publish(GatheringCreatedEvent event);
}
