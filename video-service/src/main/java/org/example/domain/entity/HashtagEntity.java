package org.example.domain.entity;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class HashtagEntity {
    private Long id;
    private String name;
    private Long usageCount;
    private LocalDateTime createdAt;

    public void incrementUsage() {
        if (this.usageCount == null) {
            this.usageCount = 0L;
        }
        this.usageCount++;
    }
}