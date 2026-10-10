package org.example.domain.entity;

import org.example.domain.exception.AccountErrorCode;
import org.example.domain.exception.AccountException;
import org.example.domain.valueobject.Email;
import org.example.domain.valueobject.Password;

import java.time.ZonedDateTime;
import java.util.Set;

public class Account {
    private Long id; // Định danh Snowflake ID
    private String username;

    // Sử dụng Value Objects cho email và password
    private Email email;
    private Password password;

    private String phoneNumber;
    private AccountStatus status;

    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Abstract hóa bảng trung gian account_roles
    private Set<Role> roles;
    private int tokenVersion;
    // Constructor phục vụ cho Mapper từ Persistence Entity lên Domain Entity
    public Account(Long id, String username, Email email, String phoneNumber,
                   Password password, AccountStatus status,
                   ZonedDateTime createdAt, ZonedDateTime updatedAt, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.status = status != null ? status : AccountStatus.INACTIVE;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
    }

    // --- CÁC HÀM XỬ LÝ NGHIỆP VỤ ---
    // Tạo tài khoản mới
    public static Account create(Long id, String username, Email email,
                                 String phoneNumber, Password encodedPassword,
                                 Set<Role> defaultRoles) {

        // Validate các rule cơ bản
        if (username == null || username.trim().isEmpty()) {
            throw new AccountException(AccountErrorCode.USER_NAME_MISSING);
        }

        ZonedDateTime now = ZonedDateTime.now();

        //Trả về đối tượng Account với các trạng thái mặc định ban đầu
        return new Account(
                id,
                username,
                email,
                phoneNumber,
                encodedPassword,                // Password ĐÃ ĐƯỢC HASH từ tầng Application
                AccountStatus.INACTIVE,         // Mặc định tài khoản mới cần xác thực (Verify)
                now,                            // createdAt
                now,                            // updatedAt
                defaultRoles
        );
    }
    public boolean isValid()
    {
        return this.status == AccountStatus.ACTIVE;
    }
    public void canLogin() {
        if (this.status == AccountStatus.INACTIVE)
            throw new AccountException(AccountErrorCode.ACCOUNT_VERIFY_NEED);
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountException(AccountErrorCode.ACCOUNT_DISABLED);
        }

    }

    public void banAccount() {
        if (this.status == AccountStatus.DELETED) {
            throw new IllegalStateException("Tài khoản đã bị xóa, không thể ban.");
        }
        this.status = AccountStatus.BANNED;
        this.updatedAt = ZonedDateTime.now();
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        if (newPhoneNumber == null || newPhoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại nhận OTP không hợp lệ.");
        }
        this.phoneNumber = newPhoneNumber;
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean hasPermission(String requiredPermission) {
        if (this.status != AccountStatus.ACTIVE) {
            return false;
        }
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(requiredPermission));
    }
    public void increaseTokenVersion()
    {
        tokenVersion += 1;
    }
    public void setStatus(AccountStatus accountStatus)
    {
        this.status = accountStatus;
    }
    // --- GETTERS ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Email getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public Password getPassword() { return password; }
    public AccountStatus getStatus() { return status; }
    public ZonedDateTime getCreatedAt() { return createdAt; }
    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public Set<Role> getRoles() { return roles; }
    public int getTokenVersion(){return tokenVersion;}
    public boolean isActive()
    {
        return !this.status.equals(AccountStatus.INACTIVE);
    }
    public void setPassword(Password password)
    {
        this.password = password;
    }
}
