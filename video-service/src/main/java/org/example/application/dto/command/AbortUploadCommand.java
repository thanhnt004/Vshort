package org.example.application.dto.command;

import java.util.UUID;

public record AbortUploadCommand(
        UUID uploadId,
        Long userId // dùng để kiểm tra quyền sở hữu phiên upload trước khi hủy
) {}