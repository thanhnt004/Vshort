package org.example.infrashtructure.persistence.mapper;

import org.example.domain.entity.VideoEntity;
import org.example.infrashtructure.persistence.entity.VideoJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VideoMapper {
    VideoEntity toDomainEntity(VideoJpaEntity video);

    VideoJpaEntity toJpaEntity(VideoEntity videoEntity);

}
