package org.example.application.dto.result;

import lombok.Builder;

@Builder
public record CompleteUploadVideoResult(
        Long videoId,
        String videoUrl,
        String status
){
}
