package com.vshort.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.port.in.CleanupExpiredUploadSessionsUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class UploadCleanupScheduler {

    private final CleanupExpiredUploadSessionsUseCase cleanupUseCase;

    // Mặc định xóa các phiên treo quá 24 giờ (có thể cấu hình trong application.yml)
    @Value("${storage.s3.cleanup.expiration-hours:24}")
    private long expirationHours;

    // Chạy định kỳ vào 03:00 sáng mỗi ngày (hoặc dùng fixedDelayString để chạy mỗi 6 tiếng)
    @Scheduled(cron = "${storage.s3.cleanup.cron:0 0 3 * * *}")
    public void runCleanupJob() {
        log.info("Kích hoạt Cron Job dọn dẹp các phiên upload quá hạn...");
        cleanupUseCase.execute(Duration.ofHours(expirationHours));
    }
}