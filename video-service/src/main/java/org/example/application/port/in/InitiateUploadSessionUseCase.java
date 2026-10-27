package org.example.application.port.in;

import org.example.application.dto.command.InitSessionCommand;
import org.example.application.dto.result.InitSessionResult;

public interface InitiateUploadSessionUseCase {
    InitSessionResult initSession(InitSessionCommand command);
}
