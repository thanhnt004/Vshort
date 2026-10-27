package org.example.application.service;

import ch.qos.logback.core.spi.ErrorCodes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.dto.command.AbortUploadCommand;
import org.example.application.port.in.AbortUploadUseCase;
import org.example.application.port.out.IStoragePort;
import org.example.application.port.out.UploadSessionRepositoryPort;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.example.infrashtructure.persistence.repository.UploadSessionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AbortUploadUseCaseImpl implements AbortUploadUseCase {
    private final UploadSessionRepositoryPort uploadSessionRepository;
    private final IStoragePort storagePort;
    @Transactional
    public void execute(AbortUploadCommand command) {
        var uploadSession = uploadSessionRepository.findById(command.uploadId()).orElseThrow(
                ()-> new BusinessException(ErrorCode.UPLOAD_SESSION_NOT_FOUND)
        );
        if (!uploadSession.getUserId().equals(command.userId())) {
            throw new AccessDeniedException("Bạn không có quyền hủy phiên upload này");
        }
        if (uploadSession.isCompleted()) {
            throw new BusinessException(ErrorCode.UPLOAD_SESSION_COMPLETED);
        }
        //Gọi xuống SeaweedFS để hủy phiên Multipart Upload và giải phóng các Part đã tải dở
        if (storagePort.isUploadSessionActive(uploadSession.getFileName(),uploadSession.getProviderId())) {
            storagePort.abortUploadSession(uploadSession.getProviderId(),uploadSession.getFileName());
            log.info("Đã hủy phiên Multipart Upload [{}] cho objectKey [{}]",
                    command.uploadId(), uploadSession.getFileName());
        } else {
            log.warn("Phiên Multipart Upload [{}] không còn tồn tại trên SeaweedFS hoặc đã bị hủy trước đó",
                    command.uploadId());
        }
        uploadSession.markAsAborted();
        uploadSessionRepository.save(uploadSession);
    }
}
