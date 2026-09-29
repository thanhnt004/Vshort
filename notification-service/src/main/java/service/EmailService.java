package service;

import java.util.Map;

public interface EmailService {
    void sendEmail(String to, String subject, String htmlTemplate, Map<String, Object> variables);
}
