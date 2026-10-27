package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.model.UploadPartModel;
import org.example.application.dto.model.UploadSessionModel;
import org.example.application.port.in.SaveUploadPartUseCase;
import org.example.application.port.out.UploadSessionRepositoryPort;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.example.response.BaseErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SaveUploadPartUseCaseImpl implements SaveUploadPartUseCase {
    private final UploadSessionRepositoryPort sessionRepository;
    @Override
    public void execute(UUID sessionId, Map<Integer, String> partNumberTagsMap) {
        UploadSessionModel session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_SESSION_NOT_FOUND));
        if (session.getStatus() != UploadSessionModel.SessionStatus.UPLOADING) {
            session.setStatus(UploadSessionModel.SessionStatus.UPLOADING);
        }
        partNumberTagsMap.forEach((partNumber, etag) -> {
            UploadPartModel newPart = UploadPartModel.builder()
                    .partNumber(partNumber)
                    .etag(etag)
                    .build();

            session.getUploadParts().removeIf(p -> p.getPartNumber().equals(partNumber));
            session.getUploadParts().add(newPart);
        });
        sessionRepository.save(session);
    }
}
