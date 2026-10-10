package org.example.application.dto.result;

import lombok.Builder;
@Builder
public record TokenResult(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType
) {
    // Compact constructor to handle the default value
    public TokenResult {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
}
