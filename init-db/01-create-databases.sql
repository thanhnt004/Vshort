-- 1. Tạo role cho Debezium (Cần quyền REPLICATION và SUPERUSER để tạo publication FOR ALL TABLES)
CREATE ROLE debezium WITH SUPERUSER REPLICATION LOGIN PASSWORD 'dbz_password';

-- 2. Tạo database auth_db
CREATE DATABASE auth_db;
CREATE DATABASE noti_db;
-- 3. Cấp quyền mức độ Database cho debezium
GRANT ALL PRIVILEGES ON DATABASE auth_db TO debezium;

-- 4. Chuyển kết nối (ngữ cảnh) vào đúng database auth_db
\c auth_db;

-- 5. Cấp quyền mức độ Schema và Bảng bên trong auth_db
GRANT USAGE ON SCHEMA public TO debezium;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO debezium;