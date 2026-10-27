package org.example.application.port.out;

import org.example.domain.entity.HashtagEntity;

import java.util.List;

public interface HashTagRepositoryPort {
    List<HashtagEntity> saveOrFindAll(List<String> strings);

    void saveAll(List<HashtagEntity> hashtags);
}
