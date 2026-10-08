
DROP TRIGGER IF EXISTS trg_email_template_content_audit ON email_template_contents;
DROP TRIGGER IF EXISTS trg_email_templates_updated_at ON email_templates;
DROP FUNCTION IF EXISTS log_email_template_content_changes();
DROP FUNCTION IF EXISTS update_timestamp_column();

DROP TABLE IF EXISTS email_template_variables CASCADE;
DROP TABLE IF EXISTS email_template_content_histories CASCADE;
DROP TABLE IF EXISTS email_template_contents CASCADE;
DROP TABLE IF EXISTS email_templates CASCADE;
-- Bảng 1: Quản lý thông tin chung của Template
CREATE TABLE email_templates (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 code VARCHAR(100) NOT NULL UNIQUE,
                                 name VARCHAR(255) NOT NULL,
                                 description TEXT,
                                 status VARCHAR(20) DEFAULT 'ACTIVE',
                                 created_by VARCHAR(100),
                                 updated_by VARCHAR(100),
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 2: Quản lý nội dung Template theo ngôn ngữ (Có Versioning)
CREATE TABLE email_template_contents (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         template_id UUID NOT NULL REFERENCES email_templates(id) ON DELETE CASCADE,
                                         locale VARCHAR(10) NOT NULL DEFAULT 'vi',
                                         subject VARCHAR(255) NOT NULL,
                                         body_html TEXT NOT NULL,
                                         body_text TEXT,
                                         version INT DEFAULT 1, -- Đánh dấu phiên bản hiện tại
                                         created_by VARCHAR(100),
                                         updated_by VARCHAR(100),
                                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                         UNIQUE (template_id, locale)
);

-- Bảng 3: Định nghĩa các biến (Variables) cần thiết để map vào Template
CREATE TABLE email_template_variables (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          template_id UUID NOT NULL REFERENCES email_templates(id) ON DELETE CASCADE,
                                          variable_name VARCHAR(100) NOT NULL,
                                          description VARCHAR(255),
                                          is_required BOOLEAN DEFAULT TRUE,
                                          UNIQUE (template_id, variable_name)
);

-- Bảng 4: Lịch sử thay đổi nội dung (Snapshot / Audit Log)
CREATE TABLE email_template_content_histories (
                                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  content_id UUID NOT NULL,
                                                  template_id UUID NOT NULL,
                                                  locale VARCHAR(10) NOT NULL,
                                                  subject VARCHAR(255) NOT NULL,
                                                  body_html TEXT NOT NULL,
                                                  body_text TEXT,
                                                  version INT NOT NULL,
                                                  action VARCHAR(20) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE'
                                                  changed_by VARCHAR(100),
                                                  changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index cho bảng lịch sử để query nhanh
CREATE INDEX idx_history_content_id ON email_template_content_histories(content_id);
CREATE INDEX idx_history_template_id ON email_template_content_histories(template_id);

-- Function A: Tự động cập nhật trường updated_at cho bảng email_templates
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_email_templates_updated_at
    BEFORE UPDATE ON email_templates
    FOR EACH ROW EXECUTE FUNCTION update_timestamp_column();

-- Function B: Tự động ghi log lịch sử, tăng version và set updated_at cho contents
CREATE OR REPLACE FUNCTION log_email_template_content_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO email_template_content_histories(
            content_id, template_id, locale, subject, body_html, body_text, version, action, changed_by
        ) VALUES (
            NEW.id, NEW.template_id, NEW.locale, NEW.subject, NEW.body_html, NEW.body_text, NEW.version, 'CREATE', NEW.created_by
        );
RETURN NEW;

ELSIF (TG_OP = 'UPDATE') THEN
        -- Tự động tăng version và gán thời gian update
        NEW.version = OLD.version + 1;
        NEW.updated_at = CURRENT_TIMESTAMP;

INSERT INTO email_template_content_histories(
    content_id, template_id, locale, subject, body_html, body_text, version, action, changed_by
) VALUES (
             NEW.id, NEW.template_id, NEW.locale, NEW.subject, NEW.body_html, NEW.body_text, NEW.version, 'UPDATE', NEW.updated_by
         );
RETURN NEW;

ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO email_template_content_histories(
            content_id, template_id, locale, subject, body_html, body_text, version, action, changed_by
        ) VALUES (
            OLD.id, OLD.template_id, OLD.locale, OLD.subject, OLD.body_html, OLD.body_text, OLD.version, 'DELETE', current_user
        );
RETURN OLD;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_email_template_content_audit
    BEFORE INSERT OR UPDATE OR DELETE ON email_template_contents
    FOR EACH ROW EXECUTE FUNCTION log_email_template_content_changes();
-- tạo dữ liệu mẫu
INSERT INTO email_templates (code, name, description, created_by)
VALUES ('USER_VERIFY_EMAIL', 'Xác thực tài khoản', 'Email gửi kèm mã/link xác thực khi người dùng đăng ký tài khoản mới', 'system_admin');
INSERT INTO email_template_contents (template_id, locale, subject, body_html, body_text, created_by)
SELECT
    id,
    'vi',
    ' Xác thực địa chỉ email của bạn',
    '<h1>Xin chào [[${username}]],</h1>
     <p>Cảm ơn bạn đã đăng ký tài khoản Vshort.</p>
     <p>Vui lòng click vào đường link xác thực dưới đây để hoàn tất quá trình đăng ký:</p>
     <h2 style="color: #ff0000; letter-spacing: 2px;">http://localhost:8100/api/v1/auth/verify-email?token=[[${verifytoken}]]</h2>
     <p><i>Lưu ý: Mã xác thực này có hiệu lực trong vòng 15 phút.</i></p>',
    'Xin chào [[${username}]], Cảm ơn bạn đã đăng ký tài. Đường link xác thực của bạn là: http://localhost:8100/api/v1/auth/verify-email?token=[[${verifytoken}]]. (Mã có hiệu lực trong vòng 15 phút)',
    'system_admin'
FROM email_templates WHERE code = 'USER_VERIFY_EMAIL';

INSERT INTO email_template_variables (template_id, variable_name, description, is_required)
SELECT id, 'username', 'Tên đăng nhập hoặc Họ tên người dùng', TRUE FROM email_templates WHERE code = 'USER_VERIFY_EMAIL'
UNION ALL
SELECT id, 'verifytoken', 'Chuỗi Token để xác thực', TRUE FROM email_templates WHERE code = 'USER_VERIFY_EMAIL';
