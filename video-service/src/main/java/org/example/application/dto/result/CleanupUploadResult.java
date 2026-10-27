package org.example.application.dto.result;

public record CleanupUploadResult(
        int totalChecked,
        int successfullyAborted,
        int failedCount
) {}