package org.example.application.port.out;

import org.example.application.dto.model.UploadSessionModel;

import java.util.Optional;
import java.util.UUID;

public interface UploadSessionRepositoryPort {
    UploadSessionModel save(UploadSessionModel uploadSession);
    Optional<UploadSessionModel> findById(UUID id);
    void delete(UploadSessionModel uploadSession);
}
