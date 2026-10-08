package org.example.dto;

public record SendForgotPasswordPayload(
        String email,
        String userName,
        String token
) {
}