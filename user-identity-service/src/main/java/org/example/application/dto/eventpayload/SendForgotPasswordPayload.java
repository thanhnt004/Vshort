package org.example.application.dto.eventpayload;

public record SendForgotPasswordPayload(
        String email,
        String userName,
        String token
) {
}
