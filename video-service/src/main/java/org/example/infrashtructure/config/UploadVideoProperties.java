package org.example.infrashtructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.Set;

@Configuration
@Getter
@Component
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadVideoProperties {
    private Set<String> allowedMimeTypes;
    private DataSize maxFileSize;
    private Set<String> allowedExtensions;
}
