package org.example.infrashtructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.FileValidatePort;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.example.infrashtructure.config.UploadVideoProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileValidateHandler implements FileValidatePort {
    private final UploadVideoProperties uploadVideoProperties;
    @Override
    public void validateFile(long fileSize, String contentType, String fileName) {
        if (fileSize <= 0) {
            throw new BusinessException(ErrorCode.INVALID_FILE_SIZE);
        }
        if (fileSize > uploadVideoProperties.getMaxFileSize().toBytes()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
        if (!uploadVideoProperties.getAllowedMimeTypes().contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (!uploadVideoProperties.getAllowedExtensions().contains(fileName.substring(fileName.lastIndexOf(".") + 1))) {
            throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
        }
    }
}
