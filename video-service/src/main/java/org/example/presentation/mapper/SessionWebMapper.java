package org.example.presentation.mapper;

import org.example.application.dto.command.AbortUploadCommand;
import org.example.application.dto.command.InitSessionCommand;
import org.example.application.dto.result.InitSessionResult;
import org.example.presentation.request.AbortUploadRequest;
import org.example.presentation.request.InitUploadSessionRequest;
import org.example.presentation.response.InitUploadSessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionWebMapper {
    @Mapping(target = "userId", source = "userId")
    InitSessionCommand toCommand(InitUploadSessionRequest request, String userId);
    InitUploadSessionResponse toResponse(InitSessionResult result);
    @Mapping(target = "userId", source = "userId")
    AbortUploadCommand toCommand(AbortUploadRequest request,Long userId);
}
