package org.example.application.port.in;

import java.util.Map;

public interface VerifyEmailUseCase {
    Map<String, Object> verifyEmail(String token);
    void resendVerifyEmail(String email);
}
