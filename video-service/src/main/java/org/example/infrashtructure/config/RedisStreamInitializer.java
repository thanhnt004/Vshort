package org.example.infrashtructure.queue;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.infrashtructure.config.RedisStreamProps;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamInitializer{
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisStreamProps props;
    @PostConstruct
    public void init() {
        ensureGroup(props.getVideoJobStream(),    props.getVideoJobGroup());
    }

    private void ensureGroup(String stream, String group) {
        try {
            stringRedisTemplate.opsForStream().createGroup(stream, ReadOffset.from("0"), group);
            log.info("Created group {} on {}", group, stream);
        } catch (RedisSystemException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("BUSYGROUP")) {
                log.debug("Group {} already exists on {}", group, stream);
            } else {
                throw ex;
            }
        }
    }
}
