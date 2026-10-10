package org.example.domain.valueobject;

import org.example.constant.RegexConstants;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email {
    private final String value;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexConstants.EMAIL_PATTERN);
    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Định dạng email không hợp lệ.");
        }
        this.value = value.toLowerCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}