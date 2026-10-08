package org.example.presentation.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FieldMatchValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldMatch.List.class)
public @interface FieldMatch {
    String message() default "Mật khẩu xác thực không khớp!";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String field() default "password";
    String verifyField() default "confirmPassword";

    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FieldMatch[] value();
    }
}
