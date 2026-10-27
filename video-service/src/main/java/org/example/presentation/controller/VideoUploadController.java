package org.example.presentation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.command.AbortUploadCommand;
import org.example.application.dto.command.CompleteUploadCommand;
import org.example.application.dto.command.ResumeUploadCommand;
import org.example.application.dto.result.CleanupUploadResult;
import org.example.application.dto.result.ResumeUploadResult;
import org.example.application.port.in.*;
import org.example.application.service.CompleteUploadUseCaseImpl;
import org.example.presentation.mapper.SessionWebMapper;
import org.example.presentation.mapper.VideoWebMapper;
import org.example.presentation.request.AbortUploadRequest;
import org.example.presentation.request.CompleteUploadRequest;
import org.example.presentation.request.GetBatchPresignUrlsRequest;
import org.example.presentation.request.InitUploadSessionRequest;
import org.example.presentation.response.InitUploadSessionResponse;
import org.example.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/video-upload")
public class VideoUploadController {
    private final InitiateUploadSessionUseCase initSessionUseCase;
    private final GetPresignUrlsUseCase getPresignUrlsUseCase;
    private final SaveUploadPartUseCase saveUploadPartUseCase;
    private final CompleteUploadUseCaseImpl completeUploadUseCase;
    private final ResumeUploadUseCase resumeUploadUseCase;
    private final AbortUploadUseCase abortUploadUseCase;
    private final CleanupExpiredUploadSessionsUseCase cleanupExpiredUploadSessionsUseCase;
    private final SessionWebMapper sessionWebMapper;
    private final VideoWebMapper videoWebMapper;
    @PostMapping("/init")
    public ResponseEntity<ApiResponse<InitUploadSessionResponse>> initUploadSession(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody InitUploadSessionRequest request
    ) {
        var response = initSessionUseCase.initSession(sessionWebMapper.toCommand(request, jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success(sessionWebMapper.toResponse(response)));
    }
    @GetMapping("/presign-urls")
    public ResponseEntity<ApiResponse<?>> getUploadUrls(@Valid @RequestBody GetBatchPresignUrlsRequest request
                                                           ) {
        return ResponseEntity.ok(ApiResponse.success(getPresignUrlsUseCase.getBatchChunkUploadUrls(request.uploadSessionId(), request.partNumbers())));
    }
    @PutMapping("/{sessionId}/parts")
    public ResponseEntity<ApiResponse<?>> uploadPart(@PathVariable UUID sessionId,
                                                     @RequestBody Map<Integer,String> partNumberTagsMap
                                                     ) {
        saveUploadPartUseCase.execute(sessionId, partNumberTagsMap);
        return ResponseEntity.ok(ApiResponse.success());
    }
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<?>> completeUpload(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable UUID sessionId,
                                                         @RequestBody CompleteUploadRequest request
                                                         ) {
        var result = completeUploadUseCase.execute(sessionId, videoWebMapper.toCommand(request, jwt.getSubject()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    @PostMapping("/resume")
    public ResponseEntity<ResumeUploadResult> resumeUpload(@RequestBody ResumeUploadCommand command) {
        ResumeUploadResult result = resumeUploadUseCase.execute(command);
        return ResponseEntity.ok(result);
    }
    @DeleteMapping("/abort")
    public ResponseEntity<Void> abortUpload(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AbortUploadRequest request) {
        abortUploadUseCase.execute(sessionWebMapper.toCommand(request, Long.valueOf(jwt.getSubject())));
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/cleanup")
    public ResponseEntity<CleanupUploadResult> triggerCleanup(
            @RequestParam(defaultValue = "24") long olderThanHours) {
        CleanupUploadResult result = cleanupExpiredUploadSessionsUseCase.execute(Duration.ofHours(olderThanHours));
        return ResponseEntity.ok(result);
    }
}
