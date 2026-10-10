package org.example.consumer;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AccountCreatedEvent;
import org.example.entity.EmailTemplateContent;
import org.example.event.DebeziumEvent;
import org.example.event.OutboxEntity;
import org.example.repository.EmailTemplateContentRepository;
import org.example.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {
    private final EmailService emailService;
    private final EmailTemplateContentRepository templateContentRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "auth_cdc.public.out_box_event_jpa_entity", groupId = "noti-group")
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
                        log.info("Processing new account event: {}, email: {}", accountEvent.getUsername(), accountEvent.getEmail());

                        EmailTemplateContent template = templateContentRepository
                                .findByTemplateCodeAndLocale("USER_VERIFY_EMAIL", "vi")
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy template USER_VERIFY_EMAIL"));

                        Map<String, Object> variables = new HashMap<>();
                        variables.put("username", accountEvent.getUsername());
                        variables.put("verifytoken", accountEvent.getEmailVerifyToken());

                        // Gửi email
                        emailService.sendEmail(
                                accountEvent.getEmail(),
                                template.getSubject(),
                                template.getBodyHtml(),
                                variables
                        );
                        log.info("Email verification sent successfully to: {}", accountEvent.getEmail());
                    }
                    case "PASSWORD_RESET_REQUESTED", "PasswordResetRequested", "password_reset_requested" -> {
                        log.info("Password reset requested event received");
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
