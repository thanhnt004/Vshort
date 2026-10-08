package org.example.presentation.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Objects;

public class FieldNotMatchValidator implements ConstraintValidator<FieldNotMatch, Object> {
    private String field;
    private String verifyField;

    @Override
    public void initialize(FieldNotMatch constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.verifyField = constraintAnnotation.verifyField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Object firstValue = new BeanWrapperImpl(value).getPropertyValue(field);
            Object verifyFieldValue = new BeanWrapperImpl(value).getPropertyValue(verifyField);

            if (firstValue == null && verifyFieldValue == null) {
                return true;
            }

            boolean isValid = !Objects.equals(firstValue, verifyFieldValue);

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(verifyField)
                        .addConstraintViolation();
            }

            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}
