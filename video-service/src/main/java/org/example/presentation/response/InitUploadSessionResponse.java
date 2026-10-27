package org.example.presentation.response;


import lombok.Builder;

import java.util.UUID;

@Builder
public record InitUploadSessionResponse(
        UUID sessionId,
        long totalChunks,
        long chunkSize,
        int recommendedPresignBatchSize
) {

}