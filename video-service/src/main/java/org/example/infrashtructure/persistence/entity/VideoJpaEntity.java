package org.example.infrashtructure.persistence.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "videos", indexes = {
        @Index(name = "idx_videos_user_id", columnList = "user_id, created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoJpaEntity {

    @Id
    private Long id; // Dùng Snowflake ID

    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "video_title", nullable = false)
    private String videoTitle;
    @Column(length = 2200)
    private String description;

    @Column(name = "storage_provider", length = 50)
    private String storageProvider;
    @Column(name = "bucket_name", length = 100)
    private String bucketName;
    @Column(name = "video_key", length = 500)
    private String videoKey;
    @Column(name = "cover_key", length = 500)
    private String coverKey;

    @Column(name = "file_size")
    private Long fileSize;
    @Column(length = 50)
    private String format = "video/mp4";
    @Column(nullable = false)
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_status", length = 20)
    private ContentStatus contentStatus = ContentStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_status", length = 20)
    private PrivacyStatus privacyStatus = PrivacyStatus.PUBLIC;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Quan hệ 1-1 với VideoStats
    @OneToOne(mappedBy = "video", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private VideoStatJpaEntity videoStat;

    // Quan hệ n-n với Hashtag
    @ManyToMany
    @JoinTable(
            name = "video_hashtags",
            joinColumns = @JoinColumn(name = "video_id"),
            inverseJoinColumns = @JoinColumn(name = "hashtag_id")
    )
    private Set<HashtagJpaEntity> hashtags = new HashSet<>();
}