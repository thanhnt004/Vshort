package org.example.domain.entity;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class VideoEntity {
    private Long id;
    private Long userId;
    private String description;
    private String videoTitle;

    private String storageProvider;
    private String bucketName;
    private String videoKey;
    private String coverKey;

    private Long fileSize;
    private String format;
    private Integer duration;

    private ContentStatus contentStatus;
    private PrivacyStatus privacyStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các thành phần phụ thuộc của Video aggregate
    private VideoStatEntity stat;
    private List<Long> hashtagIds; // Chỉ lưu ID của hashtag

    public enum ContentStatus { ACTIVE, UNDER_REVIEW, BANNED }
    public enum PrivacyStatus { PUBLIC, PRIVATE, FRIENDS_ONLY }
    // BUSINESS LOGIC & STATE MUTATIONS
    public void banVideo() {
        this.contentStatus = ContentStatus.BANNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePrivacy(PrivacyStatus newPrivacy) {
        this.privacyStatus = newPrivacy;
        this.updatedAt = LocalDateTime.now();
    }

    public void addHashtagId(Long hashtagId) {
        if (this.hashtagIds == null) {
            this.hashtagIds = new ArrayList<>();
        }
        if (!this.hashtagIds.contains(hashtagId)) {
            this.hashtagIds.add(hashtagId);
        }
    }
}
