package org.example.application.port.in;

import org.example.application.dto.command.LoginCommand;
import org.example.application.dto.result.TokenResult;

public interface LoginAccountUseCase {
    TokenResult execute(LoginCommand loginCommand);
}
