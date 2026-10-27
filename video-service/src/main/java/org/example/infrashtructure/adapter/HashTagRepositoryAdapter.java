package org.example.infrashtructure.adapter;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.application.port.out.HashTagRepositoryPort;
import org.example.domain.entity.HashtagEntity;
import org.example.infrashtructure.persistence.entity.HashtagJpaEntity;
import org.example.infrashtructure.persistence.mapper.HashTagMapper;
import org.example.infrashtructure.persistence.repository.HashTagJpaRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class HashTagRepositoryAdapter implements HashTagRepositoryPort {
    private final HashTagJpaRepository hashTagRepository;
    private final HashTagMapper hashTagMapper;
    @Override
    @Transactional
    public List<HashtagEntity> saveOrFindAll(List<String> hashTags) {
        if (hashTags == null || hashTags.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalizedTags = hashTags.stream()
                .filter(Objects::nonNull)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        if (normalizedTags.isEmpty()) {
            return Collections.emptyList();
        }

        // lấy tất cả các hashtag đã tồn tại
        List<HashtagJpaEntity> existingEntities = hashTagRepository.findAllByNameIn(normalizedTags);

        Set<String> existingNames = existingEntities.stream()
                .map(HashtagJpaEntity::getName)
                .collect(Collectors.toSet());

        // Lọc ra các hashtag chưa có trong DB và tạo mới
        List<HashtagJpaEntity> newEntities = normalizedTags.stream()
                .filter(name -> !existingNames.contains(name))
                .map(HashtagJpaEntity::new)
                .toList();

        // Lưu hàng loạt (Batch Insert) trong 1 lần gọi nếu có hashtag mới
        List<HashtagJpaEntity> savedNewEntities = newEntities.isEmpty()
                ? Collections.emptyList()
                : hashTagRepository.saveAll(newEntities);

        //Gộp ID của các hashtag cũ và mới rồi trả về
        return Stream.concat(existingEntities.stream(), savedNewEntities.stream())
                .map(hashTagMapper::toDomainEntity)
                .toList();
    }

    @Override
    public void saveAll(List<HashtagEntity> hashtags) {
        hashTagRepository.saveAll(hashtags.stream().map(hashTagMapper::toJpaEntity).toList());
    }
}
