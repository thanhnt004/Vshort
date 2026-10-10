package org.example.application.dto.result;

public record RegisterResult(
        String userId,
        String username,
        String email
) {
}