package org.example.presentation.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FieldNotMatchValidator.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldNotMatch.List.class)
public @interface FieldNotMatch {
    String message() default "Hai trường giá trị không được trùng nhau!";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String field() default "password";
    String verifyField() default "newPassword";

    @Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FieldNotMatch[] value();
    }
}
