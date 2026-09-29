package org.example.infrastructure.persistence.mapper;

import org.example.domain.entity.Permission;
import org.example.infrastructure.persistence.entity.PermissionJpaEntity;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toDomainEntity(PermissionJpaEntity jpaEntity);
    PermissionJpaEntity toJpaEntity(Permission domainEntity);
    Set<Permission> toDomainEntities(Set<PermissionJpaEntity> jpaEntities);
    Set<PermissionJpaEntity> toJpaEntities(Set<Permission> domainEntities);
}
