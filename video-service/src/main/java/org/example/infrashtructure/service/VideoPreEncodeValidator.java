package org.example.infrashtructure.queue;

import lombok.extern.slf4j.Slf4j;
import org.example.infrashtructure.config.VideoValidationConfig;

import org.example.infrashtructure.persistence.entity.UploadSessionRedisEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class VideoPreEncodeValidator {

    private final FfprobeService ffprobeService;
    private final VideoValidationConfig.VideoValidationProperties props;
    private final Semaphore processSemaphore;
    private final ThreadPoolTaskExecutor validationExecutor;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    public VideoPreEncodeValidator(
            FfprobeService ffprobeService,
            VideoValidationConfig.VideoValidationProperties props,
            @Qualifier("videoValidationExecutor") ThreadPoolTaskExecutor validationExecutor) {
        this.ffprobeService = ffprobeService;
        this.props = props;
        this.validationExecutor = validationExecutor;
        this.processSemaphore = new Semaphore(props.maxConcurrentProbes(), true);
    }

    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        shuttingDown.set(true);
    }

    /**
     * Gọi đồng bộ ngay trước hàm encode (phù hợp cho REST API, Virtual Threads hoặc Consumer Kafka/RabbitMQ).
     */
    public VideoMetadata validateBeforeEncode(String filerPath) {
        if (shuttingDown.get()) {
            throw new PreEncodeValidationException(
                    FailureCategory.SERVER_BUSY,
                    "Hệ thống đang tắt để bảo trì, tạm ngưng nhận yêu cầu kiểm tra video"
            );
        }

        boolean acquired;
        try {
            acquired = processSemaphore.tryAcquire(props.acquireSlotTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PreEncodeValidationException(FailureCategory.INFRA_TEMPORARY_ERROR, "Luồng kiểm tra bị ngắt", e);
        }

        if (!acquired) {
            throw new PreEncodeValidationException(
                    FailureCategory.SERVER_BUSY,
                    "Hệ thống đang bận kiểm tra nhiều video cùng lúc, vui lòng thử lại sau ít giây"
            );
        }

        try {
            VideoMetadata metadata = probeWithShortRetry(filerPath);
            checkBusinessRules(metadata);
            return metadata;
        } finally {
            processSemaphore.release();
        }
    }

    /**
     * Gọi bất đồng bộ nếu muốn nối chuỗi trực tiếp sang pipeline encode trên nền.
     */
    public CompletableFuture<VideoMetadata> validateBeforeEncodeAsync(String filerPath) {
        return CompletableFuture.supplyAsync(() -> validateBeforeEncode(filerPath), validationExecutor);
    }

    private VideoMetadata probeWithShortRetry(String filerPath) {
        Map<String, String> headers = (props.filerJwt() == null || props.filerJwt().isBlank())
                ? Collections.emptyMap()
                : Map.of("Authorization", "Bearer " + props.filerJwt());

        int maxAttempts = Math.max(1, props.maxTransientRetries() + 1);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return ffprobeService.probeFilerPath(filerPath, headers);
            } catch (FfprobeService.FfprobeException e) {
                boolean canRetry = e.isRetryable() && attempt < maxAttempts && !shuttingDown.get();
                if (canRetry) {
                    log.warn("Lỗi mạng tạm thời khi probe {} (lần {}/{}), thử lại sau {}ms: {}",
                            filerPath, attempt, maxAttempts, attempt * 400L, e.getMessage());
                    sleepQuietly(attempt * 400L);
                    continue;
                }

                FailureCategory category = switch (e.getErrorType()) {
                    case INVALID_MEDIA, PROBE_INCOMPLETE, INVALID_URL -> FailureCategory.INVALID_VIDEO;
                    case NOT_FOUND -> FailureCategory.FILE_NOT_FOUND;
                    default -> FailureCategory.INFRA_TEMPORARY_ERROR;
                };
                throw new PreEncodeValidationException(category, e.getMessage(), e);
            }
        }
        throw new PreEncodeValidationException(FailureCategory.INFRA_TEMPORARY_ERROR, "Không thể đọc thông số video");
    }

    private void checkBusinessRules(VideoMetadata meta) {
        VideoValidationConfig.VideoValidationProperties.Rules rules = props.rules();
        List<String> violations = new ArrayList<>();

        // 1. Chặn file không xác định được thời lượng (tránh bom thời lượng làm treo ffmpeg)
        if (meta.durationSeconds() <= 0) {
            violations.add("Không xác định được thời lượng video (file chưa hoàn chỉnh hoặc thiếu header)");
        } else if (meta.durationSeconds() > rules.maxDurationSeconds()) {
            violations.add(String.format("Thời lượng %.1fs vượt quá giới hạn %.0fs",
                    meta.durationSeconds(), rules.maxDurationSeconds()));
        }

        // 2. Kiểm tra độ phân giải theo chiều hiển thị thực tế
        int w = meta.displayWidth();
        int h = meta.displayHeight();
        int shortEdge = Math.min(w, h);
        int longEdge = Math.max(w, h);
        int minAllowedShort = Math.min(rules.minWidth(), rules.minHeight());
        int maxAllowedLong = Math.max(rules.maxWidth(), rules.maxHeight());
        int maxAllowedShort = Math.min(rules.maxWidth(), rules.maxHeight());

        if (shortEdge < minAllowedShort) {
            violations.add(String.format("Độ phân giải %dx%d quá thấp (cạnh ngắn tối thiểu %dpx)",
                    w, h, minAllowedShort));
        }
        if (longEdge > maxAllowedLong || shortEdge > maxAllowedShort) {
            violations.add(String.format("Độ phân giải %dx%d vượt quá mức tối đa %dx%d",
                    w, h, rules.maxWidth(), rules.maxHeight()));
        }

        // 3. Kiểm tra tốc độ khung hình
        if (meta.fps() <= 0) {
            violations.add("Không xác định được tốc độ khung hình (FPS)");
        } else if (meta.fps() > rules.maxFps()) {
            violations.add(String.format("Tốc độ khung hình %.2f fps vượt quá mức cho phép %.0f fps",
                    meta.fps(), rules.maxFps()));
        }

        // 4. Kiểm tra codec đầu vào
        if (rules.allowedVideoCodecs() != null && !rules.allowedVideoCodecs().isEmpty()
                && !rules.allowedVideoCodecs().contains(meta.videoCodec())) {
            violations.add("Định dạng nén video '" + meta.videoCodec() + "' không được hỗ trợ");
        }
        if (rules.allowedAudioCodecs() != null && !rules.allowedAudioCodecs().isEmpty()
                && !rules.allowedAudioCodecs().contains(meta.audioCodec())) {
            violations.add("Định dạng nén audio '" + meta.audioCodec() + "' không được hỗ trợ");
        }
        if (rules.allowedFormats() != null && !rules.allowedFormats().isEmpty()
                && !rules.allowedFormats().contains(meta.containerFormat())) {
            violations.add("Định dạng video '" + meta.containerFormat() + "' không được hỗ trợ");
        }
        // 5. Kiểm tra luồng âm thanh nếu nghiệp vụ bắt buộc
        if (rules.requireAudio() && !meta.hasAudio()) {
            violations.add("Video bắt buộc phải có luồng âm thanh");
        }
        if (!violations.isEmpty()) {
            throw new PreEncodeValidationException(
                    FailureCategory.RULE_VIOLATION,
                    String.join("; ", violations)
            );
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new PreEncodeValidationException(
                    FailureCategory.INFRA_TEMPORARY_ERROR,
                    "Luồng chờ retry bị ngắt",
                    ie
            );
        }
    }

    public enum FailureCategory {
        INVALID_VIDEO,         // File hỏng, giả mạo video -> trả 400 & xóa ngay file gốc trên SeaweedFS
        RULE_VIOLATION,        // Vượt độ phân giải, quá dài, sai codec -> trả 422 & xóa file gốc
        FILE_NOT_FOUND,        // Không thấy file trên Filer -> trả 404
        SERVER_BUSY,           // Quá tải slot ffprobe -> trả 429 hoặc giữ lại message trong queue
        INFRA_TEMPORARY_ERROR  // Lỗi mạng/SeaweedFS 5xx -> trả 503 hoặc cho queue retry
    }

    public static class PreEncodeValidationException extends RuntimeException {
        private final FailureCategory category;

        public PreEncodeValidationException(FailureCategory category, String message) {
            super(message);
            this.category = category;
        }

        public PreEncodeValidationException(FailureCategory category, String message, Throwable cause) {
            super(message, cause);
            this.category = category;
        }

        public FailureCategory getCategory() {
            return category;
        }

        public boolean shouldDeleteSourceFile() {
            return category == FailureCategory.INVALID_VIDEO || category == FailureCategory.RULE_VIOLATION;
        }
    }
}