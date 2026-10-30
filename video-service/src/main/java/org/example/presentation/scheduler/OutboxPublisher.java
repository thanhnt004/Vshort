package org.example.infrashtructure.queue;

import ch.qos.logback.core.spi.ErrorCodes;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.dto.model.VideoEncodeJob;
import org.example.domain.exception.BusinessException;
import org.example.domain.exception.ErrorCode;
import org.example.infrashtructure.persistence.entity.OutBoxEventJpaEntity;
import org.example.infrashtructure.persistence.repository.OutBoxEventRepository;
import org.example.infrashtructure.persistence.repository.VideoJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutBoxEventRepository outboxRepo;
    private final RedisVideoJobProducer producer;
    private final ObjectMapper objectMapper;
    private final VideoPreEncodeValidator validator;
    private final VideoJpaRepository videoRepository;
    @Scheduled(fixedDelayString = "${outbox.poll-ms:1000}")
    @Transactional
    public void publishPending() {

        List<OutBoxEventJpaEntity> batch =
                outboxRepo.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();
        for (OutBoxEventJpaEntity e : batch) {
            try {
                if (e.getAttempts() >= 3)
                    continue;
                VideoEncodeJob job =
                        objectMapper.readValue(e.getPayload(), VideoEncodeJob.class);
                var meta = validator.validateBeforeEncode("buckets/"+job.rawBucket()+"/"+job.rawKey());
                var video = videoRepository.findById(job.videoId()).orElseThrow(
                        ()-> new BusinessException(ErrorCode.VIDEO_NOT_FOUND)
                );
                if (video.getFileSize()!=meta.sizeBytes())
                    throw new BusinessException(ErrorCode.VIDEO_SIZE_MISMATCH);
                if (video.getDuration()!=meta.durationSeconds())
                {
                    videoRepository.updateDuration(job.videoId(), meta.durationSeconds());
                }
                producer.enqueue(job);
                e.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                if (e.getAttempts() >= 3)
                {
                    // dead-letter
                    log.error("Publish outbox failed id={} attempts={}",
                            e.getId(), e.getAttempts(), ex);
                }else
                {
                    e.setAttempts(e.getAttempts() + 1);
                    log.warn("Publish outbox failed id={} attempts={}",
                            e.getId(), e.getAttempts(), ex);
                }
            }
        }
    }
}