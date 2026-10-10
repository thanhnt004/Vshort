package org.example.application.port.out;

import org.example.application.dto.eventpayload.ResendVerifyEmailPayload;
import org.example.event.EventMetadata;
import org.example.event.EventWrapper;

public interface EventPublisherPort {
    void publishEvent( EventWrapper message,String topic);
}
