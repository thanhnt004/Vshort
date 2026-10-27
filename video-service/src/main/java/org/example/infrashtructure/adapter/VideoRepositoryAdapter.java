package org.example.infrashtructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.VideoRepositoryPort;
import org.example.domain.entity.VideoEntity;
import org.example.infrashtructure.persistence.mapper.VideoMapper;
import org.example.infrashtructure.persistence.repository.VideoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VideoRepositoryAdapter implements VideoRepositoryPort {
    private final VideoJpaRepository videoJpaRepository;
    private final VideoMapper videoMapper;
    @Override
    public VideoEntity save(VideoEntity video) {
        return videoMapper.toDomainEntity(videoJpaRepository.save(videoMapper.toJpaEntity(video)));
    }

    @Override
    public Optional<VideoEntity> findById(Long id) {
        return Optional.ofNullable(videoMapper.toDomainEntity(videoJpaRepository.findById(id).orElse(null)));
    }

    @Override
    public void delete(VideoEntity video) {
        videoJpaRepository.delete(videoMapper.toJpaEntity(video));
    }
}
