package org.example.application.port.in;

import org.example.application.dto.command.ChangePasswordCommand;
import org.example.application.dto.command.ResetPasswordCommand;

public interface ManagePasswordUseCase {
    void changePassword(ChangePasswordCommand changePasswordCommand);
    void sendForgotPasswordEmail(String email);
    void validateForgotPasswordToken(String token);
    void resetPassword(ResetPasswordCommand resetPasswordCommand);
}
