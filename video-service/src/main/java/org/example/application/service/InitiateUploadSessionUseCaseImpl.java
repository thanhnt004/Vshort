package org.example.application.service;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.command.InitSessionCommand;
import org.example.application.dto.model.UploadSessionModel;
import org.example.application.dto.result.InitSessionResult;
import org.example.application.port.in.InitiateUploadSessionUseCase;
import org.example.application.port.out.*;
import org.example.infrashtructure.adapter.ChunkSizeCalculateHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InitiateUploadSessionUseCaseImpl implements InitiateUploadSessionUseCase {
    private final FileValidatePort fileValidatePort;
    private final ChunkSizeCalculatePort chunkSizeCalculatePort;
    private final UploadSessionRepositoryPort uploadSessionRepositoryPort;
    private final IStoragePort storagePort;
    @Override
    public InitSessionResult initSession(InitSessionCommand command){
        //validate
        fileValidatePort.validateFile(command.fileSize(),command.format(), command.fileName());
        //calculate chunk sizes and number
        ChunkSizeCalculateHandler.ChunkPlan chunkPlan = chunkSizeCalculatePort.calculateOptimalPlan(command.fileSize());

        //create new session
        String uniqueFileName = generateUniqueFileName(command.fileName());// avoid name conflict
        String providerID = storagePort.initUploadSession(uniqueFileName,command.format());
        var newSession = UploadSessionModel.builder()
                                .providerId(providerID)
                                .fileName(uniqueFileName)
                                .fileSize(command.fileSize())
                                .duration(command.duration())
                                .storageProvider("S3MinO")
                                .userId(command.userId())
                                .format(command.format())
                                .totalParts(chunkPlan.totalChunks())
                                .chunkSize(chunkPlan.chunkSize())
                                .status(UploadSessionModel.SessionStatus.INITIATED)
                                .uploadParts(new ArrayList<>())
                                .build();
        newSession = uploadSessionRepositoryPort.save(newSession);

        return InitSessionResult.builder()
                .sessionId(newSession.getId())
                .totalChunks(newSession.getTotalParts())
                .chunkSize(chunkPlan.chunkSize())
                .recommendedPresignBatchSize(chunkPlan.recommendedPresignBatchSize())
                .build();
    }
    private String generateUniqueFileName(String originalName) {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + "-" + originalName;
    }

}
