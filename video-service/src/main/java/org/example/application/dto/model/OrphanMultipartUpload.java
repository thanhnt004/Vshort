package org.example.application.dto.model;

import java.time.Instant;

public record OrphanMultipartUpload(
        String uploadId,
        String objectKey,
        Instant initiatedAt
) {}
