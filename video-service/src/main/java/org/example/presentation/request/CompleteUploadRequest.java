package org.example.presentation.request;

import jakarta.validation.constraints.NotBlank;

public record CompleteUploadRequest(
        @NotBlank
        String videoTitle,
        String videoDescription,
        String videoThumbUrl,
        @NotBlank
        String expectedTags
) {
}
