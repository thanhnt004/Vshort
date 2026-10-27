package org.example.infrashtructure.persistence.repository;

import org.example.infrashtructure.persistence.entity.ContentStatus;
import org.example.infrashtructure.persistence.entity.PrivacyStatus;
import org.example.infrashtructure.persistence.entity.VideoJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoJpaRepository extends JpaRepository<VideoJpaEntity, Long> {
    // Tìm danh sách video của một user, sắp xếp mới nhất (hỗ trợ phân trang)
    Page<VideoJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Tìm video public theo ID
    Optional<VideoJpaEntity> findByIdAndPrivacyStatusAndContentStatus(
            Long id,
            PrivacyStatus privacyStatus,
            ContentStatus contentStatus
    );
}
