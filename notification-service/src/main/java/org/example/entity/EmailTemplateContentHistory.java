package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_template_content_histories", indexes = {
        @Index(name = "idx_history_content_id", columnList = "content_id"),
        @Index(name = "idx_history_template_id", columnList = "template_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateContentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Sử dụng UUID trực tiếp thay vì quan hệ Entity để tối ưu query lịch sử và tránh lỗi cascade
    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @Column(name = "template_id", nullable = false)
    private UUID templateId;

    @Column(name = "locale", nullable = false, length = 10)
    private String locale;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body_html", nullable = false, columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "changed_by", length = 100)
    private String changedBy;

    // Database tự sinh khi trigger chạy
    @Column(name = "changed_at", insertable = false, updatable = false)
    private Instant changedAt;
}