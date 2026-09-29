package org.example.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.port.out.TokenHandlerPort;
import org.example.infrastructure.cache.RefreshTokenCacheAdapter;
import org.example.infrastructure.config.TokenType;
import org.example.infrastructure.security.JwtTokenProvider;
import org.example.utils.CryptoUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenHandler implements TokenHandlerPort {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenCacheAdapter refreshTokenCacheAdapter;
    private static final long GRACE_PERIOD_MS = 10000;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;
    private static final long ACCESS_TOKEN_EXPIRATION = 36000;
    @Override
    public String generateAccessToken(String userId, Set<String> roleNames,Set<String> permissionNames) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("roles", roleNames);
        claims.put("permissions",permissionNames);
        return jwtTokenProvider.createToken(TokenType.ACCESS,userId,claims);
    }

    @Override
    public String generateRefreshToken(String userId, String familyId,String tokenVersion) {
        String rawToken = CryptoUtils.generateSecureTokenRaw();
        String tokenHash = CryptoUtils.hash(rawToken);
        refreshTokenCacheAdapter.saveNewToken(tokenHash,userId,familyId,REFRESH_TOKEN_VALIDITY_DAYS,tokenVersion);
        return rawToken;
    }

    @Override
    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION;
    }

    @Override
    public String rotateTokens(String hashedOldToken, String newAccessToken,String userId, String familyId,String tokenVersion) {
        String rawToken = CryptoUtils.generateSecureTokenRaw();
        String tokenHash = CryptoUtils.hash(rawToken);
        //cap nhap token cu voi GracePeriod
        refreshTokenCacheAdapter.markTokenAsUsedWithGracePeriod(
                hashedOldToken,          // Key cũ (đã hash)
                newAccessToken,          // Trả về raw cho client đến sau
                rawToken,// Trả về raw moi cho client đến sau
                GRACE_PERIOD_MS
        );
        //luu token moi
        refreshTokenCacheAdapter.saveNewToken(
                tokenHash,
                userId,
                familyId,
                REFRESH_TOKEN_VALIDITY_DAYS,
                tokenVersion
        );
        return rawToken;
    }

    @Override
    public boolean tokenIsValid(Map<String, String> tokenData) {
        if (tokenData == null || tokenData.isEmpty()) {
           return false;
        }
        if (refreshTokenCacheAdapter.isFamilyCompromised(tokenData.get("family_id"))) {
            return false;
        }
        return true;
    }
}
