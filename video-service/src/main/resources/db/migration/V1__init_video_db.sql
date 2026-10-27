-- 1. Hàm và Trigger tự động cập nhật updated_at cho PostgreSQL
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Bảng lưu trữ thông tin chính của Video
CREATE TABLE videos (
                        id BIGINT PRIMARY KEY, -- Dùng Snowflake ID
                        user_id BIGINT NOT NULL, -- ID của người đăng
                        description VARCHAR(2200), -- Nội dung caption
                        video_title VARCHAR(500), -- Thêm dấu phẩy bị thiếu

    -- TÁCH BIỆT STORAGE VÀ GIAO THỨC TRUYỀN TẢI
                        storage_provider VARCHAR(50), -- VD: 'AWS_S3', 'MINIO', 'CLOUDFLARE_R2', 'LOCAL'
                        bucket_name VARCHAR(100), -- Tên bucket chứa file
                        video_key VARCHAR(500), -- Đường dẫn tương đối (Object Key) của file fMP4
                        cover_key VARCHAR(500), -- Đường dẫn tương đối của ảnh Thumbnail

    -- THÔNG TIN FILE CHO STREAMING (Byte-Range)
                        file_size BIGINT, -- Kích thước file (byte)
                        format VARCHAR(50) DEFAULT 'video/mp4',
                        duration INT NOT NULL, -- Thời lượng video (giây)

    -- TÁCH TRẠNG THÁI UPLOAD VÀ TRẠNG THÁI HIỂN THỊ
                        upload_status VARCHAR(20) DEFAULT 'PENDING',
                        content_status VARCHAR(20) DEFAULT 'ACTIVE',
                        privacy_status VARCHAR(20) DEFAULT 'PUBLIC',

                        created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Gán trigger cập nhật updated_at cho bảng videos
CREATE TRIGGER trg_videos_updated_at
    BEFORE UPDATE ON videos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_videos_user_id ON videos(user_id, created_at DESC);

-- 3. Bảng lưu thống kê
CREATE TABLE video_stats (
                             video_id BIGINT PRIMARY KEY REFERENCES videos(id) ON DELETE CASCADE,
                             view_count BIGINT DEFAULT 0,
                             like_count BIGINT DEFAULT 0,
                             comment_count BIGINT DEFAULT 0,
                             share_count BIGINT DEFAULT 0
);

-- 4. Bảng quản lý Hashtag
CREATE TABLE hashtags (
                          id BIGINT PRIMARY KEY,
                          name VARCHAR(100) UNIQUE NOT NULL,
                          usage_count BIGINT DEFAULT 0,
                          created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 5. Bảng N-N nối Video và Hashtag
CREATE TABLE video_hashtags (
                                video_id BIGINT NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
                                hashtag_id BIGINT NOT NULL REFERENCES hashtags(id) ON DELETE CASCADE,
                                PRIMARY KEY (video_id, hashtag_id)
);

CREATE INDEX idx_hashtag ON video_hashtags(hashtag_id);