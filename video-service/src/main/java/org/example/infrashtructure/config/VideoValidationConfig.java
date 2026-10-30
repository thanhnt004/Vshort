package org.example.infrashtructure.config;

import com.example.video.FfprobeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(VideoValidationConfig.VideoValidationProperties.class)
public class VideoValidationConfig {

    private static final Logger log = LoggerFactory.getLogger(VideoValidationConfig.class);

    @ConfigurationProperties(prefix = "app.video-validation")
    public record VideoValidationProperties(
            String ffprobePath,
            String filerBaseUrl,
            String filerJwt,
            long timeoutSeconds,
            long rwTimeoutMicros,
            long probeSizeBytes,
            long analyzeDurationMicros,
            int maxConcurrentProbes,
            long acquireSlotTimeoutSeconds,
            int maxTransientRetries,
            Rules rules
    ) {
        public record Rules(
                double maxDurationSeconds,
                int minWidth,
                int minHeight,
                int maxWidth,
                int maxHeight,
                double maxFps,
                boolean requireAudio,
                Set<String> allowedVideoCodecs
        ) {}
    }

    @Bean(destroyMethod = "close")
    public FfprobeService ffprobeService(VideoValidationProperties props, ObjectMapper objectMapper) {
        URI filerUri = (props.filerBaseUrl() == null || props.filerBaseUrl().isBlank())
                ? null
                : URI.create(props.filerBaseUrl());
        return new FfprobeService(
                props.ffprobePath(),
                filerUri,
                props.timeoutSeconds(),
                props.rwTimeoutMicros(),
                props.probeSizeBytes(),
                props.analyzeDurationMicros(),
                objectMapper
        );
    }

    /**
     * ThreadPool tùy chọn nếu bạn muốn chạy bước validate -> encode bất đồng bộ trong nền.
     */
    @Bean(name = "videoValidationExecutor")
    public ThreadPoolTaskExecutor videoValidationExecutor(VideoValidationProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.maxConcurrentProbes());
        executor.setMaxPoolSize(props.maxConcurrentProbes());
        executor.setQueueCapacity(props.maxConcurrentProbes() * 4);
        executor.setThreadNamePrefix("video-validate-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds((int) props.timeoutSeconds() + 5);
        executor.initialize();
        return executor;
    }

    @Bean
    public ApplicationRunner verifyFfprobeOnStartup(VideoValidationProperties props) {
        return args -> {
            Process p = new ProcessBuilder(props.ffprobePath(), "-version").start();
            if (!p.waitFor(5, TimeUnit.SECONDS) || p.exitValue() != 0) {
                p.destroyForcibly();
                throw new IllegalStateException("Không tìm thấy hoặc không chạy được ffprobe tại: " + props.ffprobePath());
            }
            log.info("Sẵn sàng validate video qua ffprobe (tối đa {} tiến trình song song)", props.maxConcurrentProbes());
        };
    }
}