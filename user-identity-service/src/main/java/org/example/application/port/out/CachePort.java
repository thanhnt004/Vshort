package org.example.application.port.out;

import java.util.Optional;

public interface CachePort {
    void set(String key, Object value, long ttlInSeconds);
    Optional<Object> get(String key);
    void delete(String key);
}
