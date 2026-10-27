package org.example.application.dto.command;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CompleteUploadCommand(
        Long userId,
        String videoTitle,
        String videoDescription,
        String videoThumbUrl,
        String expectedTags,
        List<String> hashTags
) {
}