package org.example.application.dto.command;

import java.util.List;

public record InitSessionCommand
        (
                Long userId,
                String fileName,
                long fileSize,
                long chunkSize,
                String format,
                int duration
        ){
}
