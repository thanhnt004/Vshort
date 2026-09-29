package org.example.constant;

public final class RegexConstants {
    private RegexConstants() {} // Ngăn không cho khởi tạo object
    public static final String USERNAME_PATTERN = "^(?![0-9]+$)[a-zA-Z0-9_]{3,50}$";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_VN_PATTERN = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$";
    public static final String PASSWORD_STRONG = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
}