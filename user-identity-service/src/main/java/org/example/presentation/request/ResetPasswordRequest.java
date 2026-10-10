package org.example.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.constant.RegexConstants;
import org.example.presentation.validate.FieldMatch;

@FieldMatch
public record ResetPasswordRequest(@NotBlank
                                   String token,
                                   @NotBlank(message = "Mật khẩu không được để trống")
                                   @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
                                   @Pattern(regexp = RegexConstants.PASSWORD_STRONG,message = "Mật khẩu chưa đủ mạnh!")
                                   String password,
                                   @NotBlank(message = "Xác nhận mật khẩu không được để trống")
                                   String confirmPassword

) {

}
