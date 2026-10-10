package org.example.presentation.mapper;

import org.example.application.dto.command.ChangePasswordCommand;
import org.example.application.dto.command.ResetPasswordCommand;
import org.example.presentation.request.ChangePasswordRequest;
import org.example.presentation.request.ResetPasswordRequest;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PasswordWebMapper {
    @Mapping(target = "userId", source = "subject")
    @Mapping(target = "currentPassword", source = "request.password")
    ChangePasswordCommand toCommand(ChangePasswordRequest request, @Nullable String subject);
    @Mapping(target = "newPassword",source = "request.password")
    ResetPasswordCommand toCommand(ResetPasswordRequest request);
}
