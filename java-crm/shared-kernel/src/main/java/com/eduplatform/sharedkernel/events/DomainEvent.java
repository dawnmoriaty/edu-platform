package com.eduplatform.sharedkernel.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();
    LocalDateTime getOccurredOn();
    String getEventType();
}

abstract class BaseDomainEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final String eventType;

    public BaseDomainEvent() {
        this(UUID.randomUUID(), LocalDateTime.now());
    }

    public BaseDomainEvent(UUID eventId, LocalDateTime occurredOn) {
        this.eventId = eventId;
        this.occurredOn = occurredOn;
        this.eventType = this.getClass().getSimpleName();
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    @Override
    public String getEventType() {
        return eventType;
    }
}
