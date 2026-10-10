package org.example.service;

import java.util.Map;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class BrevoEmailService implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public BrevoEmailService(JavaMailSender mailSender, @Qualifier("stringTemplateEngine") TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendEmail(String to, String subject, String htmlTemplate, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            String processHtml = templateEngine.process(htmlTemplate, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("nhthanh2k4@gmail.com"); //email
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(processHtml, true); // true = Bật chế độ gửi HTML
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi gửi email qua Brevo: " + e.getMessage(), e);
        }
    }
}
