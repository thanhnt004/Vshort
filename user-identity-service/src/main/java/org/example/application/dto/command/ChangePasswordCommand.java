package org.example.application.dto.command;

public record ChangePasswordCommand(
        String userId,
        String currentPassword,
        String newPassword
) {}

