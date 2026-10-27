package org.example.application.dto.command;

import java.util.UUID;

public record ResumeUploadCommand(
        UUID uploadId
) {}
