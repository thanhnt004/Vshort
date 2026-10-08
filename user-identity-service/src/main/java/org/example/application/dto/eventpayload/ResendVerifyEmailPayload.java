package org.example.application.dto.eventpayload;

public record ResendVerifyEmailPayload(String accountId,
                                       String username,
                                       String email,String verifyToken) {
}
