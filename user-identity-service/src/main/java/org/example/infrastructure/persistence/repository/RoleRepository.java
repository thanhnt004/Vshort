package org.example.infrastructure.persistence.repository;

import org.example.infrastructure.persistence.entity.RoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<RoleJpaEntity,Integer> {
    //default role: USER
    @Query(value = "SELECT * FROM roles r WHERE r.name = 'USER'", nativeQuery = true)
    RoleJpaEntity getDefaultRole();
}
