package org.example.infrashtructure.persistence.repository;

import org.example.infrashtructure.persistence.entity.SessionStatus;

import org.example.infrashtructure.persistence.entity.UploadSessionRedisEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadSessionRepository extends CrudRepository<UploadSessionRedisEntity, UUID> {
    // Tìm session theo ID
    Optional<UploadSessionRedisEntity> findById(String id);

    // Tìm các session chưa hoàn thành và đã hết hạn (để cleanup cronjob)
    List<UploadSessionRedisEntity> findByStatusAndExpiresAtBefore(SessionStatus status, LocalDateTime now);
}
