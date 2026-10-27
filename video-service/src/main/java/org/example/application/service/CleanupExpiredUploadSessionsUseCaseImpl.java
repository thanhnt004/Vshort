package org.example.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.dto.model.OrphanMultipartUpload;
import org.example.application.dto.result.CleanupUploadResult;
import org.example.application.port.in.CleanupExpiredUploadSessionsUseCase;
import org.example.application.port.out.IStoragePort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupExpiredUploadSessionsUseCaseImpl implements CleanupExpiredUploadSessionsUseCase {
    private final IStoragePort objectStoragePort;
    @Override
    public CleanupUploadResult execute(Duration retentionDuration) {
        Instant expirationThreshold = Instant.now().minus(retentionDuration);
        List<OrphanMultipartUpload> activeUploads = objectStoragePort.listInProgressMultipartUploads();
        //Lọc ra các phiên đã quá hạn (initiatedAt < expirationThreshold)
        List<OrphanMultipartUpload> expiredUploads = activeUploads.stream()
                .filter(upload -> upload.initiatedAt() != null && upload.initiatedAt().isBefore(expirationThreshold))
                .toList();

        int abortedCount = 0;
        int failedCount = 0;
        //Thực hiện hủy từng phiên trên SeaweedFS
        for (OrphanMultipartUpload expired : expiredUploads) {
            try {
                objectStoragePort.abortUploadSession( expired.uploadId(),expired.objectKey());

                abortedCount++;
                log.info("Đã dọn dẹp phiên upload quá hạn: uploadId={}, key={}, initiatedAt={}",
                        expired.uploadId(), expired.objectKey(), expired.initiatedAt());
            } catch (Exception e) {
                failedCount++;
                log.error("Lỗi khi dọn dẹp phiên upload: uploadId={}, key={}",
                        expired.uploadId(), expired.objectKey(), e);
            }
        }
        log.info("Hoàn tất dọn dẹp Multipart Upload: Kiểm tra={}, Đã xóa={}, Lỗi={}",
                activeUploads.size(), abortedCount, failedCount);

        return new CleanupUploadResult(activeUploads.size(), abortedCount, failedCount);
    }
}
