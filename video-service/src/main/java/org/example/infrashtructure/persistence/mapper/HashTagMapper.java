package org.example.infrashtructure.persistence.mapper;

import org.example.domain.entity.HashtagEntity;
import org.example.infrashtructure.persistence.entity.HashtagJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HashTagMapper {
    HashtagEntity toDomainEntity(HashtagJpaEntity hashTag);
    HashtagJpaEntity toJpaEntity(HashtagEntity hashTag);
}
