package org.example.application.dto.model;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadSessionModel {

    private UUID id;
    private Long userId;
    private String providerId; // Upload ID của S3/MinIO
    private String fileName;
    private Long fileSize;
    private String storageProvider;
    private String format;
    private int duration;
    private Integer totalParts;
    private Long chunkSize;
    private SessionStatus status;

    @Builder.Default
    private List<UploadPartModel> uploadParts = new ArrayList<>();

    public boolean isCompleted() {
        return this.status == SessionStatus.COMPLETED;
    }

    // Enum (Có thể tách ra file riêng)
    public enum SessionStatus {
        INITIATED, UPLOADING, COMPLETED, FAILED, ABORTED
    }
    public void markAsAborted() {
        this.status = SessionStatus.ABORTED;
    }
    public void markAsCompleted() {
        this.status = SessionStatus.COMPLETED;
    }
    public void markAsFailed() {
        this.status = SessionStatus.FAILED;
    }
    public void markAsInitiated() {
        this.status = SessionStatus.INITIATED;
    }
}