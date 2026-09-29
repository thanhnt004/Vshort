package org.example.presentation.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordConfirmCheck implements ConstraintValidator<PasswordsMatch, Object>{
    private String passwordField;
    private String confirmPasswordField;
    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // Đọc giá trị của 2 field từ Object truyền vào
            Object password = new BeanWrapperImpl(value).getPropertyValue(passwordField);
            Object confirmPassword = new BeanWrapperImpl(value).getPropertyValue(confirmPasswordField);

            // Logic so sánh
            boolean isValid;
            if (password == null) {
                isValid = (confirmPassword == null);
            } else {
                isValid = password.equals(confirmPassword);
            }

            // Nếu KHÔNG KHỚP, map lỗi trực tiếp vào trường "confirmPassword"
            // (Giúp Frontend dễ dàng nhận diện lỗi nằm ở ô input nào)
            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(confirmPasswordField) // Gắn lỗi vào field confirmPassword
                        .addConstraintViolation();
            }

            return isValid;

        } catch (Exception e) {
            // Nếu có lỗi (như gõ sai tên field), đánh fail luôn
            return false;
        }
    }
}
