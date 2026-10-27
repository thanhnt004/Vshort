package org.example.application.dto.result;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record InitSessionResult(
        UUID sessionId,
        long totalChunks,
        long chunkSize,
        int recommendedPresignBatchSize
) {

}
