package org.example.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.dto.result.TokenResult;
import org.example.application.port.in.RefreshTokenUseCase;
import org.example.application.port.out.AccountRepositoryPort;
import org.example.application.port.out.RefreshTokenPort;
import org.example.application.port.out.TokenHandlerPort;
import org.example.domain.entity.Account;
import org.example.domain.entity.Permission;
import org.example.domain.entity.Role;
import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.utils.CryptoUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {
    private final RefreshTokenPort refreshTokenPort;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;
    private final TokenHandlerPort tokenHandler;
    private final AccountRepositoryPort accountRepositoryPort;
    @Override
    public TokenResult execute(String refreshToken) {
        String hashedOldToken = CryptoUtils.hash(refreshToken);
        Map<String, String> tokenData = refreshTokenPort.getTokenData(hashedOldToken);
        log.info("hash token: "+ hashedOldToken+"/n raw token: "+refreshToken);
        //validate ton tai va blacklist
        if (!tokenHandler.tokenIsValid(tokenData))
            throw new AccountException(AccountErrorCode.INVALID_TOKEN);
        log.info("token data: "+ tokenData.toString());
        //lay thong tin
        String userId = tokenData.get("user_id");
        String familyId = tokenData.get("family_id");
        String status = tokenData.get("status");
        Set<String> roles = null;
        Set<String> permissions = null;
        //lay tai khoan
        Optional<Account> account = accountRepositoryPort.findById(Long.valueOf(userId));
        if (account.isPresent())
        {
            if (!account.get().isValid()) {
                // Thu hồi toàn bộ token family ngay lập tức
                refreshTokenPort.blacklistFamily(tokenData.get("family_id"), REFRESH_TOKEN_VALIDITY_DAYS);
                throw new AccountException(AccountErrorCode.ACCOUNT_DISABLED);
            }
            String cachedVersion = tokenData.get("token_version");
            if (cachedVersion == null || !cachedVersion.equals(String.valueOf(account.get().getTokenVersion()))) {
                // Phiên bản không khớp (người dùng đã ấn Logout All hoặc Đổi mật khẩu)
                refreshTokenPort.deleteToken(hashedOldToken);
                throw new AccountException(AccountErrorCode.INVALID_TOKEN);
            }
            roles = account.get().getRoles().stream().map(Role::getName).collect(Collectors.toSet());
            permissions = account.get().getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(Permission::getName)
                    .collect(Collectors.toSet());
        }
        else {
            throw new AccountException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }
        //validate used co grace
        if ("USED".equals(status)) {
            long graceExpiry = Long.parseLong(tokenData.get("grace_expiry"));

            if (System.currentTimeMillis() <= graceExpiry) {
                // Hợp lệ trong dung sai: Trả về token cache
                return TokenResult.builder()
                        .accessToken(tokenData.get("new_at"))
                        .refreshToken(tokenData.get("new_rt"))
                        .expiresIn(tokenHandler.getAccessTokenExpiration())
                        .build();
            } else {
                // phat hien tai su dung sau thoi gian cho phep
                refreshTokenPort.blacklistFamily(familyId, REFRESH_TOKEN_VALIDITY_DAYS);
                refreshTokenPort.deleteToken(hashedOldToken);
                throw new AccountException(AccountErrorCode.INVALID_TOKEN);
            }
        }
        //tao moi
        String newAccessToken = tokenHandler.generateAccessToken(userId,roles,permissions);
        String newOpaqueRefreshToken = tokenHandler.rotateTokens(hashedOldToken,newAccessToken,userId,familyId,tokenData.get("token_version"));
        return TokenResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newOpaqueRefreshToken)
                .expiresIn(tokenHandler.getAccessTokenExpiration())
                .build();
    }
}
