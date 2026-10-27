package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.command.ResumeUploadCommand;
import org.example.application.dto.model.UploadPartModel;
import org.example.application.dto.result.ResumeUploadResult;
import org.example.application.port.in.ResumeUploadUseCase;
import org.example.application.port.out.IStoragePort;
import org.example.application.port.out.UploadSessionRepositoryPort;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeUploadUseCaseImpl implements ResumeUploadUseCase {
    private final IStoragePort storagePort;
    private final UploadSessionRepositoryPort uploadSessionRepository;
    @Override
    public ResumeUploadResult execute(ResumeUploadCommand command) {
        //kiem tra phien upload co ton tai khong
        var uploadSession = uploadSessionRepository.findById(command.uploadId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_SESSION_NOT_FOUND));
        if (!storagePort.isUploadSessionActive(uploadSession.getFileName(), uploadSession.getProviderId())) {
            throw new BusinessException(ErrorCode.UPLOAD_SESSION_NOT_FOUND);
        }
        List<UploadPartModel> completedParts = uploadSession.getUploadParts();
        Set<Integer> completedPartNumbers = completedParts.stream()
                .map(UploadPartModel::getPartNumber)
                .collect(Collectors.toSet());
        List<Integer> remainingParts = new ArrayList<>();
        for (int partNum = 1; partNum <= uploadSession.getTotalParts(); partNum++) {
            if (!completedPartNumbers.contains(partNum)) {
                remainingParts.add(partNum);
            }
        }
        return new ResumeUploadResult(
                command.uploadId(),
                uploadSession.getFileName(),
                completedPartNumbers,
                remainingParts
        );
    }
}
