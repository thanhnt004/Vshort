-- 1. Thêm thông tin template vào bảng email_templates
INSERT INTO email_templates (code, name, description, created_by)
VALUES ('FORGOT_PASSWORD_EMAIL', 'Quên mật khẩu', 'Email gửi kèm link đặt lại mật khẩu khi người dùng yêu cầu', 'system_admin');

-- 2. Thêm nội dung HTML và Text vào bảng email_template_contents
INSERT INTO email_template_contents (template_id, locale, subject, body_html, body_text, created_by)
SELECT
    id,
    'vi',
    'Yêu cầu đặt lại mật khẩu',
    '<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Yêu cầu đặt lại mật khẩu</title>
    <style>
        body { font-family: ''Helvetica Neue'', Helvetica, Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
        .email-wrapper { width: 100%; background-color: #f4f7f6; padding: 40px 0; }
        .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05); overflow: hidden; }
        .email-header { background-color: #0056b3; padding: 20px; text-align: center; }
        .email-header h1 { color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; }
        .email-body { padding: 30px; color: #333333; line-height: 1.6; font-size: 16px; }
        .email-body p { margin-bottom: 20px; }
        .btn-container { text-align: center; margin: 30px 0; }
        .btn { display: inline-block; background-color: #0056b3; text-decoration: none; padding: 14px 28px; border-radius: 4px; font-weight: bold; font-size: 16px; }
        .btn:hover { background-color: #004494; }
        .email-footer { background-color: #f9f9f9; padding: 20px; text-align: center; font-size: 13px; color: #888888; border-top: 1px solid #eeeeee; }
        .email-footer p { margin: 5px 0; }
    </style>
</head>
<body>
    <div class="email-wrapper">
        <div class="email-container">
            <div class="email-header">
                <h1>Vshort</h1>
            </div>
            
            <div class="email-body">
                <p>Xin chào <strong>[[${username}]]</strong>,</p>
                <p>Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với địa chỉ email này. Vui lòng nhấn vào nút bên dưới để tiến hành thiết lập mật khẩu mới của bạn:</p>
                
                <div class="btn-container">
                    <a th:href="''http://localhost:8100/api/v1/auth/reset-password?token='' + ${token}" class="btn" target="_blank">Đặt Lại Mật Khẩu</a>
                </div>
                
                <p><em>Lưu ý: Liên kết này sẽ hết hạn sau <strong>15 phút</strong> để đảm bảo an toàn cho tài khoản của bạn.</em></p>
                <p>Nếu bạn không thực hiện yêu cầu này, bạn có thể an tâm bỏ qua email này. Tài khoản của bạn vẫn được bảo mật và không có thay đổi nào được thực hiện.</p>
                
                <p>Trân trọng,<br>
                <strong>Đội ngũ Vshort</strong></p>
            </div>
            
            <div class="email-footer">
                <p>Email này được tạo tự động, vui lòng không trả lời.</p>
                <p>Nếu bạn cần hỗ trợ, vui lòng liên hệ với chúng tôi qua <a href="mailto:support@vshort.com" style="color: #0056b3;">support@vshort.com</a>.</p>
                <p>&copy; 2026 Vshort. Tất cả các quyền được bảo lưu.</p>
            </div>
        </div>
    </div>
</body>
</html>',
    'Xin chào [[${username}]], Chúng tôi đã nhận được yêu cầu đặt lại mật khẩu của bạn. Link đặt lại mật khẩu của bạn là: http://localhost:8100/api/v1/auth/reset-password?token=[[${resettoken}]]. (Link có hiệu lực trong vòng 15 phút)',
    'system_admin'
FROM email_templates WHERE code = 'FORGOT_PASSWORD_EMAIL';

-- 3. Khai báo các biến sẽ được sử dụng trong template
INSERT INTO email_template_variables (template_id, variable_name, description, is_required)
SELECT id, 'username', 'Tên đăng nhập hoặc Họ tên người dùng', TRUE FROM email_templates WHERE code = 'FORGOT_PASSWORD_EMAIL'
UNION ALL
SELECT id, 'token', 'Chuỗi Token để đặt lại mật khẩu', TRUE FROM email_templates WHERE code = 'FORGOT_PASSWORD_EMAIL';