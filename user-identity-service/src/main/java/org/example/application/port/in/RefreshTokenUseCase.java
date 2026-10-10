package org.example.application.port.in;

import org.example.application.dto.result.TokenResult;

public interface RefreshTokenUseCase {
    TokenResult execute(String refreshToken);
}
