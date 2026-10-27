package org.example.presentation.mapper;

import org.example.application.dto.command.CompleteUploadCommand;
import org.example.presentation.request.CompleteUploadRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VideoWebMapper {
    @Mapping(target = "userId", source = "userId")
    CompleteUploadCommand toCommand(CompleteUploadRequest request,String userId);
}
