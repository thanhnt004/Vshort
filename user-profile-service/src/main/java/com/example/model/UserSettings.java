package com.example.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "language", length = 10, nullable = false)
    private String language = "vi";

    @Builder.Default
    @Column(name = "settings_mask", nullable = false)
    private Integer settingsMask = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime updatedAt;

    // Liên kết 1:1 với Profile qua PK user_id
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_settings_profile"))
    private Profile profile;

    // =========================================================================
    // HELPER METHODS THAO TÁC BITMASK (settings_mask)
    // =========================================================================

    /**
     * Bit 0: Chế độ riêng tư (0: Public, 1: Private)
     */
    public boolean isPrivateAccount() {
        return (this.settingsMask & 1) != 0;
    }

    public void setPrivateAccount(boolean isPrivate) {
        if (isPrivate) {
            this.settingsMask |= 1;  // Bật bit 0
        } else {
            this.settingsMask &= ~1; // Tắt bit 0
        }
    }

    /**
     * Bit 1,2: Theme (0: System/Default, 1: Light, 2: Dark)
     * Mask lấy giá trị 2 bit: (settingsMask >> 1) & 3
     */
    public int getThemeMode() {
        return (this.settingsMask >> 1) & 0b11;
    }

    public void setThemeMode(int themeMode) {
        // Xóa bit 1 và bit 2 cũ: AND với ~6 (110)
        this.settingsMask &= ~0b110;
        // Ghi giá trị themeMode mới vào bit 1,2
        this.settingsMask |= ((themeMode & 0b11) << 1);
    }
}