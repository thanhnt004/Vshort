package org.example.infrashtructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.dto.model.OrphanMultipartUpload;
import org.example.application.port.out.IStoragePort;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.example.infrashtructure.persistence.entity.SessionStatus;
import org.example.infrashtructure.persistence.entity.UploadSessionRedisEntity;
import org.example.infrashtructure.persistence.repository.UploadSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3StorageAdapter implements IStoragePort {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    @Value("${seaweedfs.bucket}")
    private String bucketName ;
    private final UploadSessionRepository uploadSessionRepository;
    @Override
    public String initUploadSession(String fileName,String contentType) {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
        return response.uploadId();
    }

    @Override
    public String getPresignedUrlForPart(String uploadId,String fileName, int partNumber) {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15)) // URL sống trong 15 phút
                .uploadPartRequest(uploadPartRequest)
                .build();

        // Trả về URL để Client thực hiện HTTP PUT chunk trực tiếp lên MinIO/S3
        return s3Presigner.presignUploadPart(presignRequest).url().toString();
    }

    @Override
    public String completeUploadSession(String fileName, String uploadSessionId, List<ChunkPart> parts,String expectedTag) {
        //Map các chunk
        List<CompletedPart> completedParts = parts.stream()
                .map(p -> CompletedPart.builder().partNumber(p.getPartNumber()).eTag(p.getETag()).build())
                .collect(Collectors.toList());

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        // Truyền fileName lấy từ DB vào request
        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(fileName) // <-- Sử dụng tên file truy xuất từ hệ thống của bạn
                .uploadId(uploadSessionId)
                .multipartUpload(completedMultipartUpload)
                .build();
        log.info("Complete multipart upload request: {}", request);
        var response = s3Client.completeMultipartUpload(request);
        String actualFinalEtag = response.eTag().replace("\"", "");
        if (!actualFinalEtag.equals(expectedTag)) {
            // Ra lệnh xóa file trên MinIO và yêu cầu Client upload lại
            deleteFile(fileName);
            throw new BusinessException(ErrorCode.DATA_INTEGRITY_CHECK_FAILED);
        }
        return String.format("localhost:8333/%s/%s", bucketName, fileName);
    }

    @Override
    public Map<Integer, String> getBatchChunkUploadUrls(String uploadSessionId, String fileName, TreeSet<Integer> partNumbers) {
        Map<Integer, String> presignedUrls = new LinkedHashMap<>();
        for (int i: partNumbers) {
            var url = getPresignedUrlForPart(uploadSessionId,fileName,i);
            presignedUrls.put(i, url);
        }
        return presignedUrls;
    }

    @Override
    public void deleteFile(String storageKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Đã xóa file lỗi trên MinIO thành công: {}", storageKey);

        } catch (S3Exception e) {
            log.error("Lỗi khi xóa file {} trên MinIO: {}", storageKey, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Không thể xóa file trên hệ thống lưu trữ", e);
        }
    }

    @Override
    public void abortUploadSession(String uploadSessionId, String storageKey) {
        try {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .uploadId(uploadSessionId) // providerId (s3UploadId)
                    .build();

            s3Client.abortMultipartUpload(abortRequest);
            log.info("Đã hủy phiên upload và xóa các chunk tạm của file: {}", storageKey);

        } catch (S3Exception e) {
            log.error("Lỗi khi hủy phiên upload {}: {}", uploadSessionId, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Không thể hủy phiên upload trên hệ thống lưu trữ", e);
        }
    }

    @Override
    public boolean isUploadSessionActive(String s, String providerId) {
        try {
            s3Client.listParts(ListPartsRequest.builder()
                    .bucket(bucketName)
                    .key(s)
                    .uploadId(providerId)
                    .maxParts(1)
                    .build());
            return true;
        } catch (NoSuchUploadException e) {
            return false;
        }
    }

    @Override
    public List<OrphanMultipartUpload> listInProgressMultipartUploads() {
        ListMultipartUploadsRequest request = ListMultipartUploadsRequest.builder()
                .bucket(bucketName)
                .build();

        ListMultipartUploadsResponse response = s3Client.listMultipartUploads(request);

        if (!response.hasUploads()) {
            return Collections.emptyList();
        }

        return response.uploads().stream()
                .map(upload -> new OrphanMultipartUpload(
                        upload.uploadId(),
                        upload.key(),
                        upload.initiated()
                ))
                .toList();
    }
}
