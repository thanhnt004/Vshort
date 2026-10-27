package org.example.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.dto.command.CompleteUploadCommand;
import org.example.application.dto.model.UploadSessionModel;
import org.example.application.dto.result.CompleteUploadVideoResult;
import org.example.application.port.in.CompleteUploadUseCase;
import org.example.application.port.out.*;
import org.example.domain.entity.HashtagEntity;
import org.example.domain.entity.VideoEntity;
import org.example.domain.entity.VideoStatEntity;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.example.infrashtructure.persistence.entity.SessionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class CompleteUploadUseCaseImpl implements CompleteUploadUseCase {
    private final UploadSessionRepositoryPort sessionRepository;
    private final IdGeneratorPort idGenerator;
    private final VideoRepositoryPort videoRepository;
    private final HashTagRepositoryPort hashTagRepository;
    private final IStoragePort storagePort;
    @Override
    @Transactional
    public CompleteUploadVideoResult execute(UUID sessionId, CompleteUploadCommand command) {
        //tim session
        UploadSessionModel session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UPLOAD_SESSION_NOT_FOUND));
        try {
            var chunkParts = session.getUploadParts().stream().map(IStoragePort.ChunkPart::new).toList();
            var url =  storagePort.completeUploadSession(
                    session.getFileName(),
                    session.getProviderId(),chunkParts,command.expectedTags()
            );
            if (session.getUserId()!=null&&!session.getUserId().equals(command.userId()))
            {
                throw new BusinessException(ErrorCode.USER_NOT_OWNER);
            }
            //tao id
            long videoId = idGenerator.generateId();
            VideoEntity newVideo = VideoEntity.builder()
                    .id(videoId)
                    .userId(command.userId())
                    .description(command.videoDescription())
                    .videoTitle(command.videoTitle())
                    .storageProvider(session.getStorageProvider())
                    .bucketName("videos-bucket")
                    .videoKey(url)
                    .coverKey(command.videoThumbUrl())
                    .fileSize(session.getFileSize())
                    .format(session.getFormat())
                    .duration(session.getDuration())
                    .contentStatus(VideoEntity.ContentStatus.ACTIVE)
                    .privacyStatus(VideoEntity.PrivacyStatus.PUBLIC)
                    .stat(VideoStatEntity.createNew(videoId))
                    .build();
            List<HashtagEntity> hashtags = new ArrayList<>();
            if (command.hashTags() != null)
            {
                hashtags = hashTagRepository.saveOrFindAll(command.hashTags());
            }
            for (HashtagEntity hashtag : hashtags) {
                // Gọi business logic nằm trong HashtagEntity
                hashtag.incrementUsage();

                // Gắn ID hashtag vào Video aggregate
                newVideo.addHashtagId(hashtag.getId());
            }
            hashTagRepository.saveAll(hashtags);
            videoRepository.save(newVideo);
            // Cập nhật trạng thái session trong DB thành SUCCESS
            session.setStatus(UploadSessionModel.SessionStatus.COMPLETED);
            sessionRepository.save(session);
            return CompleteUploadVideoResult.builder()
                    .videoId(videoId)
                    .videoUrl(url)
                    .status("READY")
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while completing upload {}", sessionId);
            session.markAsFailed();
            sessionRepository.save(session);
            throw e;
        }
    }
}
