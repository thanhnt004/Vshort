package org.example.application.port.out;

import java.util.Map;

public interface RefreshTokenPort {
    Map<String, String> getTokenData(String opaqueToken);
    boolean isFamilyCompromised(String familyId);
    void blacklistFamily(String familyId, long validityDays);
    void deleteToken(String opaqueToken);
    void markTokenAsUsedWithGracePeriod(String oldToken, String newAccessToken, String newRefreshToken, long gracePeriodMs);
    void saveNewToken(String newToken, String userId, String familyId, long validityDays,String tokenVersion);
}
