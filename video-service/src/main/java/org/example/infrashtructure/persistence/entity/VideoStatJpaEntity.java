package org.example.infrashtructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "video_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoStatJpaEntity {

    @Id
    @Column(name = "video_id")
    private Long videoId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Chia sẻ ID với entity Video
    @JoinColumn(name = "video_id")
    private VideoJpaEntity video;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "comment_count")
    private Long commentCount = 0L;

    @Column(name = "share_count")
    private Long shareCount = 0L;
}