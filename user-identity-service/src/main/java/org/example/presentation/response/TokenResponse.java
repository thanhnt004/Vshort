package org.example.presentation.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record TokenResponse(
        String accessToken,
        long expiresIn,
        String tokenType,
        Instant issuedAt
) {
    public TokenResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
        if (issuedAt == null)
            issuedAt = Instant.now();
    }
}
