package org.example.application.port.in;

import org.example.application.dto.command.ResumeUploadCommand;
import org.example.application.dto.result.ResumeUploadResult;

public interface ResumeUploadUseCase {
    ResumeUploadResult execute(ResumeUploadCommand command);
}
