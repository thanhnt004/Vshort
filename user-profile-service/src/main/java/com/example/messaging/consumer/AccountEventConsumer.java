package com.example.messaging.consumer;

import com.example.dto.event.AccountCreatedEvent;
import com.example.service.ProfileService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.DebeziumEvent;
import org.example.event.OutboxEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "auth_cdc.public.out_box_event_jpa_entity", groupId = "profile-group")
    public void processKafkaMessage(String kafkaJsonMessage) {
        log.info("Received Kafka message: {}", kafkaJsonMessage);
        try {
            TypeReference<DebeziumEvent<OutboxEntity>> typeRef = new TypeReference<>() {};
            DebeziumEvent<OutboxEntity> event = null;
            try {
                event = objectMapper.readValue(kafkaJsonMessage, typeRef);
            } catch (Exception e) {
                log.debug("Could not deserialize as wrapped DebeziumEvent: {}", e.getMessage());
            }

            OutboxEntity outbox = null;
            if (event != null && event.getPayload() != null) {
                DebeziumEvent.EventPayload<OutboxEntity> eventPayload = event.getPayload();
                if ("c".equals(eventPayload.getOp()) || "r".equals(eventPayload.getOp()) || "u".equals(eventPayload.getOp()) || eventPayload.getOp() == null) {
                    outbox = eventPayload.getAfter();
                }
            }

            // Trường hợp message gửi trực tiếp OutboxEntity không qua Debezium schema wrapper
            if (outbox == null) {
                try {
                    outbox = objectMapper.readValue(kafkaJsonMessage, OutboxEntity.class);
                } catch (Exception ignored) {

                }
            }

            if (outbox == null) {
                log.warn("Cannot extract OutboxEntity from message: {}", kafkaJsonMessage);
                return;
            }

            // Kiểm tra loại sự kiện và Deserialize chuỗi JSON bên trong Outbox
            if ("account".equalsIgnoreCase(outbox.getAggregateType())) {
                String eventType = outbox.getType();
                String innerJsonPayload = outbox.getPayload();
                log.info("Processing account event with type: {}", eventType);

                if (eventType == null) {
                    log.warn("Event type is null for outbox id: {}", outbox.getId());
                    return;
                }

                switch (eventType) {
                    case "ACCOUNT_CREATED", "AccountCreated", "account_created" -> {
                        AccountCreatedEvent accountEvent = objectMapper.readValue(innerJsonPayload, AccountCreatedEvent.class);
                        if (accountEvent == null || accountEvent.getAccountId() == null || accountEvent.getAccountId().isBlank()) {
                            log.warn("Invalid account event payload (missing accountId): {}", innerJsonPayload);
                            return;
                        }
                        try {
                            Long accountId = Long.valueOf(accountEvent.getAccountId().trim());
                            log.info("Processing new account event for userId: {}, username: {}, email: {}", accountId, accountEvent.getUsername(), accountEvent.getEmail());
                            profileService.createDefaultProfile(accountId, accountEvent.getUsername());
                        } catch (NumberFormatException e) {
                            log.error("Invalid accountId format in event: {}", accountEvent.getAccountId(), e);
                        }
                    }
                    default -> log.warn("Unhandled account event type: {}", eventType);
                }
            } else {
                log.debug("Ignored aggregate type: {}", outbox.getAggregateType());
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}
