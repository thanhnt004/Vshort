package org.example.application.port.out;

import org.example.domain.entity.VideoEntity;

import java.util.Optional;

public interface VideoRepositoryPort {
    VideoEntity save(VideoEntity video);
    Optional<VideoEntity> findById(Long id);
    void delete(VideoEntity video);
}
