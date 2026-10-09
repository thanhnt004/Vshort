
-- bảng thông tin tài khoản
CREATE TABLE profiles (
                          user_id BIGINT PRIMARY KEY,
                          username VARCHAR(50) UNIQUE,
                          full_name VARCHAR(100) NOT NULL,
                          avatar_url VARCHAR(500),
                          birth_date DATE,
                          bio VARCHAR(255),
                          is_verified BOOLEAN DEFAULT FALSE NOT NULL,
                          follower_count INT DEFAULT 0 NOT NULL CHECK (follower_count >= 0),
                          following_count INT DEFAULT 0 NOT NULL CHECK (following_count >= 0),
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE profiles IS 'Bảng chứa thông tin hồ sơ người dùng (User/Profile Service)';

COMMENT ON COLUMN profiles.user_id IS 'ID người dùng, khớp 1:1 với id Snowflake bên bảng accounts (auth_db)';
COMMENT ON COLUMN profiles.username IS 'Tên định danh hiển thị (@username), đồng bộ sự kiện từ auth_db';
COMMENT ON COLUMN profiles.full_name IS 'Tên hiển thị chính của người dùng (Display Name)';
COMMENT ON COLUMN profiles.avatar_url IS 'Đường dẫn ảnh đại diện lưu trữ trên CDN/S3';
COMMENT ON COLUMN profiles.bio IS 'Đoạn tiểu sử ngắn mô tả bản thân';
COMMENT ON COLUMN profiles.is_verified IS 'Trạng thái tích xanh chính chủ (TRUE/FALSE)';
COMMENT ON COLUMN profiles.follower_count IS 'Tổng số người theo dõi (Dữ liệu phi chuẩn hóa để tối ưu truy vấn)';
COMMENT ON COLUMN profiles.following_count IS 'Tổng số người đang theo dõi (Dữ liệu phi chuẩn hóa để tối ưu truy vấn)';
COMMENT ON COLUMN profiles.created_at IS 'Thời điểm tạo hồ sơ người dùng (UTC)';
COMMENT ON COLUMN profiles.updated_at IS 'Thời điểm cập nhật hồ sơ gần nhất';

-- Tìm kiếm profile nhanh theo @username
CREATE INDEX idx_profiles_username ON profiles(username) WHERE username IS NOT NULL;

-- Tối ưu cho các truy vấn gợi ý/xếp hạng người dùng có nhiều follower nhất
CREATE INDEX idx_profiles_follower_count ON profiles(follower_count DESC);

-- bảng user_settings
CREATE TABLE user_settings (
                               user_id BIGINT PRIMARY KEY,
                               language VARCHAR(10) DEFAULT 'vi' NOT NULL,
                               settings_mask INTEGER NOT NULL DEFAULT 0,--bit 0 chế độ riêng tư true false, bit 1,2 theme 1: sáng, 2 tối tương đương các giá trị 0, 1 2 4
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    -- Khóa ngoại liên kết 1:1 với bảng profiles
                               CONSTRAINT fk_user_settings_profile
                                   FOREIGN KEY (user_id) REFERENCES profiles(user_id) ON DELETE CASCADE
);

COMMENT ON TABLE user_settings IS 'Bảng lưu cài đặt ứng dụng cá nhân của người dùng (Tách riêng khỏi profiles để tối ưu truy vấn Feed)';

COMMENT ON COLUMN user_settings.user_id IS 'ID người dùng (Khóa chính & Khóa ngoại tham chiếu profiles.user_id)';
COMMENT ON COLUMN user_settings.language IS 'Ngôn ngữ giao diện ưu tiên của người dùng (Mặc định: vi. Ví dụ: vi, en, ja)';
COMMENT ON COLUMN user_settings.created_at IS 'Thời điểm khởi tạo bản ghi cài đặt';
COMMENT ON COLUMN user_settings.updated_at IS 'Thời điểm cập nhật cài đặt gần nhất';