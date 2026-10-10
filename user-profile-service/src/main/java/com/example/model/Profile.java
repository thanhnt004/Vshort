package com.example.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "profiles",
        indexes = {
                @Index(name = "idx_profiles_username", columnList = "username"),
                @Index(name = "idx_profiles_follower_count", columnList = "follower_count DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // Do tên cột SQL chứa dấu '-' nên cần bọc ngoặc kép
    @Column(name = "\"birth-date\"")
    private java.time.LocalDate birthDate;

    @Column(name = "bio", length = 255)
    private String bio;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Builder.Default
    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0;

    @Builder.Default
    @Column(name = "following_count", nullable = false)
    private Integer followingCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Quan hệ 1:1 với UserSettings (sử dụng @PrimaryKeyJoinColumn do dùng chung PK)
    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private UserSettings userSettings;

    public void setUserSettings(UserSettings userSetting)
    {
        if (userSetting == null)
        {
            if (this.userSettings != null)
                this.userSettings.setProfile(null);
        }else
            userSetting.setProfile(this);
        this.userSettings = userSetting;
    }
}