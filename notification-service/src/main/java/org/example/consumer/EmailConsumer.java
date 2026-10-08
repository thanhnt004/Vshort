package org.example.consumer;

import org.example.dto.SendForgotPasswordPayload;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.AccountCreatedEvent;
import org.example.dto.ResendVerifyEmailPayload;
import org.example.entity.EmailTemplateContent;
import org.example.event.DebeziumEvent;
import org.example.event.EventWrapper;
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
public class EmailConsumer {
    private final EmailService emailService;
    private final EmailTemplateContentRepository templateContentRepository;
    private final ObjectMapper objectMapper;
    @KafkaListener(topics = "email_events", groupId = "noti-group")
    public void processKafkaMessage(String kafkaJsonMessage) {
        log.info("Received Kafka message: {}", kafkaJsonMessage);
        try {
            EventWrapper<JsonNode> rawEvent = objectMapper.readValue(kafkaJsonMessage, new TypeReference<EventWrapper<JsonNode>>() {});
            String eventType = rawEvent.metadata().eventType();
            JsonNode rawPayload = rawEvent.payload();
            switch (eventType) {
                case "RESEND_VERIFY_EMAIL_REQUESTED" -> {
                    ResendVerifyEmailPayload payload = objectMapper.treeToValue(rawPayload, ResendVerifyEmailPayload.class);

                    EmailTemplateContent template = templateContentRepository
                            .findByTemplateCodeAndLocale("USER_VERIFY_EMAIL", "vi")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy template USER_VERIFY_EMAIL"));

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("username", payload.username());
                    variables.put("verifytoken", payload.verifyToken());

                    // Gửi email
                    emailService.sendEmail(
                            payload.email(),
                            template.getSubject(),
                            template.getBodyHtml(),
                            variables
                    );
                    log.info("Email verification sent successfully to: {}", payload.email());
                }
                case "FORGOT_PASSWORD"->{
                    SendForgotPasswordPayload payload = objectMapper.treeToValue(rawPayload, SendForgotPasswordPayload.class);
                    EmailTemplateContent template = templateContentRepository
                            .findByTemplateCodeAndLocale("FORGOT_PASSWORD_EMAIL", "vi")
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy template FORGOT_PASSWORD_EMAIL"));
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("username", payload.userName());
                    variables.put("token", payload.token());
                    log.info("reset password token: "+payload.token());
                    emailService.sendEmail(
                            payload.email(),
                            template.getSubject(),
                            template.getBodyHtml(),
                            variables
                    );
                    log.info("Email forgot password sent successfully to: {}", payload.email());
                }
                default -> {
                    log.warn("Event is not supported " + eventType);
                }
            }
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}
