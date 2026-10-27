package org.example.domain.entity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoStatEntity {
    private Long videoId;
    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;

    // Factory method để khởi tạo trạng thái ban đầu
    public static VideoStatEntity createNew(Long videoId) {
        return VideoStatEntity.builder()
                .videoId(videoId)
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .shareCount(0L)
                .build();
    }

    // ==========================================
    // BUSINESS LOGIC
    // ==========================================

    public void incrementView() {
        this.viewCount++;
    }

    public void incrementLike() {
        this.likeCount++;
    }

    public void decrementLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementComment() {
        this.commentCount++;
    }
}