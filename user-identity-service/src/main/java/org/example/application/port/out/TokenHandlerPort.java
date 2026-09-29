package org.example.application.port.out;

import java.util.Map;
import java.util.Set;

public interface TokenHandlerPort {
    String generateAccessToken(String userId, Set<String> roleNames,Set<String> permissionNames);
    String generateRefreshToken(String userId, String familyId,String tokenVersion);
    long getAccessTokenExpiration();
    String rotateTokens(String hashedOldToken, String newAccessToken,String userId, String familyId,String tokenVersion);
    boolean tokenIsValid(Map<String, String> tokenData);
}