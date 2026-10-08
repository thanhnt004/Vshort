package org.example.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.constant.RegexConstants;
import org.example.presentation.validate.FieldMatch;
import org.example.presentation.validate.FieldNotMatch;

@FieldMatch(
        field = "newPassword",
        verifyField = "confirmNewPassword",
        message = "Mật khẩu xác nhận không khớp!"
)
@FieldNotMatch(
        field = "password",
        verifyField = "newPassword",
        message = "Mật khẩu mới không được trùng với mật khẩu cũ!"
)
public record ChangePasswordRequest(
        @NotBlank(message = "Password can not blank")
        String password,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
        @Pattern(regexp = RegexConstants.PASSWORD_STRONG,message = "Mật khẩu chưa đủ mạnh!")
        String newPassword,
        @NotBlank(message = "Confirm Password can not blank")
        String confirmNewPassword
) {
}
