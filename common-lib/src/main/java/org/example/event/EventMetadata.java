package org.example.event;

import java.time.Instant;

public record EventMetadata(
        String eventId,
        String eventType,
        String source,
        String traceId,
        String userId,
        long timestamp
) {}
