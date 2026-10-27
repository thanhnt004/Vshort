package org.example.infrashtructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.model.UploadSessionModel;
import org.example.application.port.out.UploadSessionRepositoryPort;
import org.example.infrashtructure.persistence.mapper.UploadSessionMapper;
import org.example.infrashtructure.persistence.repository.UploadSessionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadSessionRepositoryAdapter implements UploadSessionRepositoryPort {
    private final UploadSessionRepository uploadSessionRepository;
    private final UploadSessionMapper uploadSessionMapper;
    @Override
    public UploadSessionModel save(UploadSessionModel uploadSession) {
        return uploadSessionMapper.toModel(uploadSessionRepository.save(uploadSessionMapper.toRedisEntity(uploadSession)));
    }

    @Override
    public Optional<UploadSessionModel> findById(UUID id) {
        return  uploadSessionRepository.findById(id).map(uploadSessionMapper::toModel);
    }

    @Override
    public void delete(UploadSessionModel uploadSession) {
        uploadSessionRepository.delete(uploadSessionMapper.toRedisEntity(uploadSession));
    }
}
