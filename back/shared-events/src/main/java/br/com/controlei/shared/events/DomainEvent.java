package br.com.controlei.shared.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public interface DomainEvent extends Serializable {
    UUID getEventId();
    Instant getOccurredOn();
    String getEventType();
    UUID getFamilyId();
}
