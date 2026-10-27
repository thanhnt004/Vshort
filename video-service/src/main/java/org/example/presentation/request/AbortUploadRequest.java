package org.example.presentation.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AbortUploadRequest (
        @NotNull
        UUID uploadId
){
}
