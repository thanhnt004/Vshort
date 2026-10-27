package org.example.infrashtructure.persistence.repository;

import org.example.infrashtructure.persistence.entity.HashtagJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HashTagJpaRepository extends JpaRepository<HashtagJpaEntity, Long> {
    List<HashtagJpaEntity> findAllByNameIn(Collection<String> names);
}
