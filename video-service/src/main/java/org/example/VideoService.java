package org.example;

import org.example.infrashtructure.config.UploadVideoProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableWebSecurity
@EnableScheduling
class VideoService {

    public static void main(String[] args) {
        SpringApplication.run(VideoService.class, args);
    }

}
