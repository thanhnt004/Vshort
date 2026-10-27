package org.example.infrashtructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RedisHash("upload_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadSessionRedisEntity {

    @Id
    private UUID id; // Redis sẽ lưu key dưới dạng: upload_sessions:{id}
    private Long userId;
    private String providerId;
    private String fileName;
    private int duration;
    private String storageProvider;
    private Integer totalParts;
    private Long chunkSize;
    private Long fileSize;
    private String status;

    // Thay thế hoàn toàn cho trường expiresAt cũ. Bạn set 86400 (24h) là an toàn.
    @TimeToLive
    @Builder.Default
    private Long timeToLiveSeconds = 86400L;

    // Nhúng trực tiếp các part vào trong session
    @Builder.Default
    private List<UploadPartRedisEntity> uploadParts = new ArrayList<>();
}