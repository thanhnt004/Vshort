package org.example.event;

public record EventWrapper<T>(
        EventMetadata metadata,
        T payload
) {}
