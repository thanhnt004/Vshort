package org.example.infrastructure.persistence.mapper;

import org.example.domain.entity.Role;
import org.example.infrastructure.persistence.entity.RoleJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toDomainEntity(RoleJpaEntity roleJpaEntity);
    RoleJpaEntity toJpaEntity(Role domainEntity);
}
