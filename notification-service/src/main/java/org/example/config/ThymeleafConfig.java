package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Bean
    @Primary
    public TemplateEngine stringTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();

        // Khai báo resolver xử lý chuỗi HTML trực tiếp từ Database
        StringTemplateResolver stringTemplateResolver = new StringTemplateResolver();
        stringTemplateResolver.setTemplateMode(TemplateMode.HTML);
        stringTemplateResolver.setCacheable(false);

        templateEngine.setTemplateResolver(stringTemplateResolver);
        return templateEngine;
    }
}