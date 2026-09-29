package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.port.in.LogoutUseCase;
import org.example.application.port.out.AccountRepositoryPort;
import org.example.application.port.out.RefreshTokenPort;
import org.example.application.port.out.TokenHandlerPort;
import org.example.domain.entity.Account;
import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class LogOutUseCaseImpl implements LogoutUseCase {
    private final RefreshTokenPort refreshTokenPort;
    private final AccountRepositoryPort accountRepositoryPort;
    private final TokenHandlerPort tokenHandler;
    @Override
    public void logout(String refreshToken) {
        String hashedToken = CryptoUtils.hash(refreshToken);
        Map<String, String> tokenData = refreshTokenPort.getTokenData(hashedToken);

        if (tokenData != null && !tokenData.isEmpty()) {
            // Chặn toàn bộ các request dùng lại token cũ của session này
            String familyId = tokenData.get("family_id");
            refreshTokenPort.blacklistFamily(familyId, 7); // 7 ngày

            // Xóa bản ghi hiện tại
            refreshTokenPort.deleteToken(hashedToken);
        }
    }

    @Override
    public void logoutAll(String refreshToken) {

        String hashedOldToken = CryptoUtils.hash(refreshToken);
        Map<String, String> tokenData = refreshTokenPort.getTokenData(hashedOldToken);
        if (tokenHandler.tokenIsValid(tokenData))
            throw new AccountException(AccountErrorCode.INVALID_TOKEN);
        Account account = accountRepositoryPort.findById(Long.valueOf(tokenData.get("userId")))
                .orElseThrow(() -> new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND));

        // Chỉ cần tăng version, MỌI refresh token cũ của user này sẽ lập tức vô hiệu hóa
        account.increaseTokenVersion();
        accountRepositoryPort.saveAccount(account);
    }
}
