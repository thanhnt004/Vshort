package org.example.application.port.in;

import org.example.application.dto.result.CleanupUploadResult;

import java.time.Duration;

public interface CleanupExpiredUploadSessionsUseCase {
    CleanupUploadResult execute(Duration retentionDuration);
}
