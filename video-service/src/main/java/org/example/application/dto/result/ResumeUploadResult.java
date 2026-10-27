package org.example.application.dto.result;

import org.example.application.dto.model.UploadPartModel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ResumeUploadResult(
        UUID uploadId,
        String objectKey,
        Set<Integer> completedParts,
        List<Integer> remainingParts
) {

}