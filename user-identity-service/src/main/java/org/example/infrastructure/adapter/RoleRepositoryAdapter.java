package org.example.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.RoleRepositoryPort;
import org.example.domain.entity.Role;
import org.example.infrastructure.persistence.mapper.RoleMapper;
import org.example.infrastructure.persistence.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepositoryPort {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    @Override
    public Role getDefailtRole() {
        return roleMapper.toDomainEntity(roleRepository.getDefaultRole());
    }
}
