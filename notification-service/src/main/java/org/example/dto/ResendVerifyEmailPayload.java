package org.example.dto;

public record ResendVerifyEmailPayload(String accountId,
                                       String username,
                                       String email, String verifyToken) {
}
