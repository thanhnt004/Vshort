package org.example.infrastructure.adapter;

import org.example.application.dto.eventpayload.ResendVerifyEmailPayload;
import org.example.application.port.out.EventPublisherPort;
import org.example.event.EventMetadata;
import org.example.event.EventWrapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventPublisherAdapter implements EventPublisherPort {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    @Override
    public void publishEvent( EventWrapper message,String topic) {
        kafkaTemplate.send(topic, message.metadata().eventId(), message);
    }

}
