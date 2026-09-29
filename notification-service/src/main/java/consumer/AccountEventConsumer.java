package consumer;

import dto.AccountCreatedEvent;
import entity.EmailTemplateContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.event.DebeziumEvent;
import org.example.event.OutboxEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import repository.EmailTemplateContentRepository;
import service.EmailService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {
    private final EmailService emailService;
    private final EmailTemplateContentRepository templateContentRepository;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    @KafkaListener(topics = "auth_cdc.public.out_box_event_jpa_entity", groupId = "noti-group")
    public void processKafkaMessage(String kafkaJsonMessage) {
        try {
            TypeReference<DebeziumEvent<OutboxEntity>> typeRef = new TypeReference<>() {
            };
            DebeziumEvent<OutboxEntity> event = objectMapper.readValue(kafkaJsonMessage, typeRef);

            DebeziumEvent.EventPayload<OutboxEntity> eventPayload = event.getPayload();
            if (eventPayload != null && ("c".equals(eventPayload.getOp()) || "r".equals(eventPayload.getOp()))) {

                OutboxEntity outbox = eventPayload.getAfter();
                // Kiểm tra loại sự kiện và Deserialize chuỗi JSON bên trong Outbox
                if ("account".equals(outbox.getAggregateType())) {
                    String eventType = outbox.getType();
                    String innerJsonPayload = outbox.getPayload();
                    switch (eventType) {
                        case "ACCOUNT_CREATED" -> {
                            AccountCreatedEvent accountEvent = objectMapper.readValue(innerJsonPayload, AccountCreatedEvent.class);
                            log.info("Processing new account: {}", accountEvent.getUsername());

                            // logic nghiệp vụ tại đây
                            EmailTemplateContent template = templateContentRepository
                                    .findByTemplateCodeAndLocale("USER_VERIFY_EMAIL", "vi")
                                    .orElseThrow(() -> new RuntimeException("Không tìm thấy template USER_VERIFY_EMAIL"));
                            Map<String, Object> variables = new HashMap<>();
                            variables.put("username", accountEvent.getUsername());
                            // Giả sử AccountCreatedEvent có chứa trường verificationToken được tạo từ lúc đăng ký
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
                        case "PASSWORD_RESET_REQUESTED" ->{

                        }
                        default -> log.warn("Unhandled account event type: {}", eventType);
                    }
                }
            }
            // Bắt lỗi JacksonException
            } catch (JacksonException e) {
                System.err.println("JSON parse error: " + e.getMessage());
                // Xử lý đưa vào Dead Letter Queue (DLQ) cho lỗi format dữ liệu
            } catch (Exception e) {
                System.err.println("System error processing message: " + e.getMessage());
                // Xử lý Dead Letter Queue (DLQ) cho các lỗi hệ thống khác
            }
    }
}
