package org.example.application.port.in;

import org.example.application.dto.command.RegisterCommand;
import org.example.application.dto.result.RegisterResult;

public interface RegisterAccountUseCase {
    RegisterResult execute(RegisterCommand command);
}
