-- Bảng tài khoản
CREATE TABLE accounts (
                          id BIGINT PRIMARY KEY,
                          username VARCHAR(50) UNIQUE,
                          email VARCHAR(255) UNIQUE,
                          phone_number VARCHAR(20) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          token_version int default 0,
                          status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL
                              CHECK (status IN ('INACTIVE','ACTIVE', 'BANNED', 'SUSPENDED','DELETED')),
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--index
CREATE INDEX idx_accounts_username ON accounts(username) WHERE username IS NOT NULL;
CREATE INDEX idx_accounts_email ON accounts(email) WHERE email IS NOT NULL;
CREATE INDEX idx_accounts_phone ON accounts(phone_number) WHERE phone_number IS NOT NULL;
--note
COMMENT ON TABLE accounts IS 'Bảng lưu trữ thông tin tài khoản và xác thực người dùng';
COMMENT ON COLUMN accounts.id IS 'Định danh người dùng duy nhất (Snowflake ID sinh từ Application)';
COMMENT ON COLUMN accounts.username IS 'Tên đăng nhập (Có thể đồng bộ sang User Service)';
COMMENT ON COLUMN accounts.email IS 'Email dùng để đăng nhập hoặc khôi phục mật khẩu';
COMMENT ON COLUMN accounts.phone_number IS 'Số điện thoại dùng để đăng nhập hoặc nhận mã OTP';
COMMENT ON COLUMN accounts.password_hash IS 'Mật khẩu đã được mã hóa an toàn (Bcrypt/Argon2)';
COMMENT ON COLUMN accounts.status IS 'Trạng thái tài khoản: ACTIVE (Hoạt động), BANNED (Cấm), SUSPENDED (Tạm đình chỉ),DELETED(xóa bởi user)';
COMMENT ON COLUMN accounts.created_at IS 'Thời điểm khởi tạo tài khoản (UTC)';
COMMENT ON COLUMN accounts.updated_at IS 'Thời điểm cập nhật thông tin tài khoản gần nhất';

--Bảng Vai trò
CREATE TABLE roles (
                       id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       description VARCHAR(255)
);

COMMENT ON TABLE roles IS 'Bảng danh mục các vai trò trong hệ thống';
COMMENT ON COLUMN roles.id IS 'Định danh vai trò (Mã số tự tăng)';
COMMENT ON COLUMN roles.name IS 'Tên vai trò duy nhất (Ví dụ: ADMIN, USER, MODERATOR)';
COMMENT ON COLUMN roles.description IS 'Mô tả chi tiết về vai trò';

--Bảng quyền hạn
CREATE TABLE permissions (
                             id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             module VARCHAR(50)
);

COMMENT ON TABLE permissions IS 'Bảng danh mục quyền thao tác trong hệ thống';
COMMENT ON COLUMN permissions.id IS 'Định danh quyền hạn (Mã số tự tăng)';
COMMENT ON COLUMN permissions.name IS 'Tên quyền duy nhất (Ví dụ: user:read, user:write, order:delete)';
COMMENT ON COLUMN permissions.module IS 'Tên phân hệ/chức năng chứa quyền (Ví dụ: USER_MANAGEMENT, BILLING)';


-- Bảng liên kết Vai trò - Quyền hạn (role_permissions)
CREATE TABLE role_permissions (
                                  role_id INT NOT NULL,
                                  permission_id INT NOT NULL,
                                  PRIMARY KEY (role_id, permission_id),
                                  CONSTRAINT fk_role_permissions_role
                                      FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_role_permissions_permission
                                      FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

COMMENT ON TABLE role_permissions IS 'Bảng trung gian liên kết giữa Vai trò (Role) và Quyền (Permission)';
COMMENT ON COLUMN role_permissions.role_id IS 'ID vai trò (Khóa ngoại tham chiếu bảng roles)';
COMMENT ON COLUMN role_permissions.permission_id IS 'ID quyền hạn (Khóa ngoại tham chiếu bảng permissions)';

--Tìm những Role nào có chứa Permission X
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);
--Bảng liên kêt n-n tài khoản - vai trò
CREATE TABLE account_roles (
                               account_id BIGINT NOT NULL,
                               role_id INT NOT NULL,
                               assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
                               PRIMARY KEY (account_id, role_id),
                               CONSTRAINT fk_account_roles_account
                                   FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE,
                               CONSTRAINT fk_account_roles_role
                                   FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Thêm ghi chú chi tiết
COMMENT ON TABLE account_roles IS 'Bảng trung gian gán Vai trò (Role) cho Tài khoản (Account)';
COMMENT ON COLUMN account_roles.account_id IS 'ID tài khoản (Snowflake ID - Tham chiếu bảng accounts)';
COMMENT ON COLUMN account_roles.role_id IS 'ID vai trò (Tham chiếu bảng roles)';
COMMENT ON COLUMN account_roles.assigned_at IS 'Thời điểm tài khoản được gán vai trò này';

-- Index tối ưu truy vấn ngược: Tìm tất cả Tài khoản có chứa Role X
CREATE INDEX idx_account_roles_role_id ON account_roles(role_id);

-- Khởi tạo danh sách Roles
INSERT INTO roles (name, description) VALUES
                                          ('USER', 'Người dùng tiêu chuẩn (có thể xem, tương tác và đăng video)'),
                                          ('VERIFIED', 'Nhà sáng tạo nội dung đã xác minh (KOL)'),
                                          ('MODERATOR', 'Kiểm duyệt viên (xử lý báo cáo, xóa video vi phạm)'),
                                          ('SUPER_ADMIN', 'Quản trị viên hệ thống (toàn quyền)');

-- Khởi tạo danh sách Permissions
INSERT INTO permissions (name, module) VALUES
-- Module Video
('video:read', 'VIDEO'),
('video:create', 'VIDEO'),
('video:delete_own', 'VIDEO'),
('video:delete_any', 'VIDEO'),
-- Module Tương tác
('interaction:like', 'INTERACTION'),
('interaction:comment', 'INTERACTION'),
('interaction:delete_own', 'INTERACTION'),
('interaction:delete_any', 'INTERACTION'),
-- Module Quản trị Người dùng
('user:update_profile', 'USER_MANAGEMENT'),
('user:ban', 'USER_MANAGEMENT'),
('user:verify', 'USER_MANAGEMENT'),
-- Module Hệ thống
('system:assign_role', 'SYSTEM'),
('system:config', 'SYSTEM');

-- Gán Permissions cho từng Role (Sử dụng CTE để ánh xạ không cần ID cứng)
WITH role_perm_mapping AS (
    -- Gán quyền cho USER
    SELECT 'USER' as r_name, 'video:read' as p_name UNION ALL
    SELECT 'USER', 'video:create' UNION ALL
    SELECT 'USER', 'video:delete_own' UNION ALL
    SELECT 'USER', 'interaction:like' UNION ALL
    SELECT 'USER', 'interaction:comment' UNION ALL
    SELECT 'USER', 'interaction:delete_own' UNION ALL
    SELECT 'USER', 'user:update_profile' UNION ALL

    -- Gán quyền cho VERIFIED (Kế thừa User, thường có thêm quyền gắn link affiliate/livestream - mô phỏng dùng chung bộ quyền User tạm)
    SELECT 'VERIFIED', 'video:read' UNION ALL
    SELECT 'VERIFIED', 'video:create' UNION ALL
    SELECT 'VERIFIED', 'video:delete_own' UNION ALL
    SELECT 'VERIFIED', 'interaction:like' UNION ALL
    SELECT 'VERIFIED', 'interaction:comment' UNION ALL
    SELECT 'VERIFIED', 'interaction:delete_own' UNION ALL
    SELECT 'VERIFIED', 'user:update_profile' UNION ALL

    -- Gán quyền cho MODERATOR (Quyền kiểm duyệt)
    SELECT 'MODERATOR', 'video:read' UNION ALL
    SELECT 'MODERATOR', 'video:delete_any' UNION ALL
    SELECT 'MODERATOR', 'interaction:delete_any' UNION ALL
    SELECT 'MODERATOR', 'user:ban' UNION ALL

    -- Gán quyền cho SUPER_ADMIN (Tất cả quyền)
    SELECT 'SUPER_ADMIN', name FROM permissions
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM role_perm_mapping m
         JOIN roles r ON r.name = m.r_name
         JOIN permissions p ON p.name = m.p_name
ON CONFLICT DO NOTHING;
