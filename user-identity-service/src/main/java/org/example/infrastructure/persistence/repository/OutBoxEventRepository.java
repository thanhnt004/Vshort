package org.example.infrastructure.persistence.repository;

import org.example.infrastructure.persistence.entity.OutBoxEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutBoxEventRepository extends JpaRepository<OutBoxEventJpaEntity, UUID> {

}
