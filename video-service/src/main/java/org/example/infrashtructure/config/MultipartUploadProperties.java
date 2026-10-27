package org.example.infrashtructure.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.storage.multipart")
public class MultipartUploadProperties {
    // Các ngưỡng giới hạn chuẩn của S3 / MinIO
    @NotNull
    private DataSize minPartSize;
    @NotNull
    private DataSize basePartSize;
    @NotNull
    private DataSize optimalMaxPartSize ;
    @NotNull
    private DataSize hardMaxPartSize ;
    @NotNull
    private DataSize singleUploadThreshold ;
    @NotNull
    private DataSize maxFileSize ;

    // Các ngưỡng số lượng Part và căn chỉnh bộ nhớ
    @NotNull
    private DataSize alignmentBlock ;
    @Min(1)
    @Max(10000)
    private int targetParts;
    @Min(1)
    @Max(10000)
    private int maxPartsLimit;
}
