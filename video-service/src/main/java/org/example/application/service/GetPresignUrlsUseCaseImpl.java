package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.model.UploadSessionModel;
import org.example.application.port.in.GetPresignUrlsUseCase;
import org.example.application.port.out.IStoragePort;
import org.example.application.port.out.UploadSessionRepositoryPort;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GetPresignUrlsUseCaseImpl implements GetPresignUrlsUseCase {
    private final IStoragePort storagePort;
    private final UploadSessionRepositoryPort sessionRepository;
    @Override
    public Map<Integer, String> getBatchChunkUploadUrls(UUID uploadSessionId, TreeSet<Integer> partNumbers) {
        UploadSessionModel session = sessionRepository.findById(uploadSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_SESSION_NOT_FOUND));
        if (partNumbers.getFirst()<=0)
            throw new BusinessException(ErrorCode.BATCH_INVALID);
        if (partNumbers.last()>session.getTotalParts())
            throw new BusinessException(ErrorCode.BATCH_INVALID);
        return storagePort.getBatchChunkUploadUrls(
                session.getProviderId(), // s3UploadId
                session.getFileName(),   // storageKey
                partNumbers
        );
    }
}
