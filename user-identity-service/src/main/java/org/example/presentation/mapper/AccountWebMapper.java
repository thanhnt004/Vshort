package org.example.presentation.mapper;

import org.example.application.dto.command.LoginCommand;
import org.example.application.dto.command.RegisterCommand;
import org.example.application.dto.result.TokenResult;
import org.example.presentation.request.LoginRequest;
import org.example.presentation.request.RegisterRequest;
import org.example.presentation.response.TokenResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountWebMapper {
    //register
    RegisterCommand toCommand(RegisterRequest request);
    //login
    LoginCommand toCommand(LoginRequest loginRequest);
    TokenResponse toResponse(TokenResult loginResult);
}
