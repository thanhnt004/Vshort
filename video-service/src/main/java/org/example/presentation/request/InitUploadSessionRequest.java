package org.example.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record InitUploadSessionRequest
        (
                String fileName,//for validate
                long fileSize,
                long chunkSize,
                String format,
                int duration
        ){
}
