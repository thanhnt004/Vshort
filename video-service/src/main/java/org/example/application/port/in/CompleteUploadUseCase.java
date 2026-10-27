package org.example.application.port.in;

import org.example.application.dto.command.CompleteUploadCommand;
import org.example.application.dto.result.CompleteUploadVideoResult;
import org.example.presentation.request.CompleteUploadRequest;

import java.util.UUID;

public interface CompleteUploadUseCase {
    CompleteUploadVideoResult execute(UUID sessionId, CompleteUploadCommand command);
}
