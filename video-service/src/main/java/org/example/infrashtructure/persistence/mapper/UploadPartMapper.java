package org.example.infrashtructure.persistence.mapper;

import org.example.application.dto.model.UploadPartModel;
import org.example.infrashtructure.persistence.entity.UploadPartRedisEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UploadPartMapper {

    UploadPartRedisEntity toRedisEntity(UploadPartModel uploadPart);

    UploadPartModel toModel(UploadPartRedisEntity uploadPartEntity);
}
