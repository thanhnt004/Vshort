package org.example.infrashtructure.persistence.mapper;

import org.example.application.dto.model.UploadSessionModel;
import org.example.infrashtructure.persistence.entity.UploadSessionRedisEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UploadSessionMapper {
    UploadSessionModel toModel(UploadSessionRedisEntity uploadSessionEntity);

    UploadSessionRedisEntity toRedisEntity(UploadSessionModel uploadSession);
}
