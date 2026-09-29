package org.example.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.RefreshTokenPort;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenCacheAdapter implements RefreshTokenPort {

    private final RedisTemplate<String,Object> redisTemplate;
    private final HashOperations<String, String, String> hashOps;

    private static final String RT_PREFIX = "rt:";
    private static final String BLACKLIST_PREFIX = "rt_family_blacklist:";

    public RefreshTokenCacheAdapter(RedisTemplate<String,Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }

    // Lấy thông tin của Token
    public Map<String, String> getTokenData(String opaqueToken) {
        return hashOps.entries(RT_PREFIX + opaqueToken);
    }

    // Kiểm tra Family có nằm trong danh sách đen không
    public boolean isFamilyCompromised(String familyId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + familyId));
    }

    // Đưa Family vào danh sách đen (Thu hồi toàn bộ)
    public void blacklistFamily(String familyId, long validityDays) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + familyId,
                "COMPROMISED",
                validityDays,
                TimeUnit.DAYS
        );
    }

    // Xóa một Token cụ thể
    public void deleteToken(String opaqueToken) {
        redisTemplate.delete(RT_PREFIX + opaqueToken);
    }

    // Cập nhật Token thành USED và lưu đệm Token mới cho Grace Period
    public void markTokenAsUsedWithGracePeriod(String oldToken, String newAccessToken, String newRefreshToken, long gracePeriodMs) {
        String rtKey = RT_PREFIX + oldToken;
        Map<String, String> updates = new HashMap<>();
        updates.put("status", "USED");
        updates.put("grace_expiry", String.valueOf(System.currentTimeMillis() + gracePeriodMs));
        updates.put("new_at", newAccessToken);
        updates.put("new_rt", newRefreshToken);

        hashOps.putAll(rtKey, updates);
        // Không gọi set expire ở đây để giữ nguyên TTL cũ của token
    }

    // Lưu Token mới hoàn toàn
    public void saveNewToken(String newToken, String userId, String familyId, long validityDays,String tokenVersion) {
        String rtKey = RT_PREFIX + newToken;
        Map<String, String> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("family_id", familyId);
        data.put("token_version", tokenVersion);
        data.put("status", "ACTIVE");

        hashOps.putAll(rtKey, data);
        redisTemplate.expire(rtKey, validityDays, TimeUnit.DAYS);
    }
}