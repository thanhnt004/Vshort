package org.example.application.dto.command;

public record ResetPasswordCommand(String token,
                                   String newPassword
                                   ) {
}
