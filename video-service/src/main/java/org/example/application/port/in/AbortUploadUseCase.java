package org.example.application.port.in;

import org.example.application.dto.command.AbortUploadCommand;

public interface AbortUploadUseCase {
    void execute(AbortUploadCommand command);
}
