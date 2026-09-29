package service;

import java.util.Map;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequiredArgsConstructor
@Service
public class BrevoEmailService implements EmailService{
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Override
    public void sendEmail(String to, String subject, String htmlTemplate, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            String processHtml = templateEngine.process(htmlTemplate, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("noreply@sellico.vn"); //email
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(processHtml, true); // true = Bật chế độ gửi HTML
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hệ thống khi gửi email qua Brevo: "+e);
        }
    }
}
