package org.example.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import org.example.application.port.out.OutBoxEventRepositoryPort;
import org.example.infrastructure.persistence.entity.OutBoxEventJpaEntity;
import org.example.infrastructure.persistence.repository.OutBoxEventRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutBoxEventRepositoryAdapter implements OutBoxEventRepositoryPort {
    private final OutBoxEventRepository jpaRepository;
    @Override
    public void saveEvent(String aggregateType, String aggregateId, String payload,String type) {
        OutBoxEventJpaEntity entity = OutBoxEventJpaEntity.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .type(type)
                .payload(payload)
                .createAt(Instant.now())
                .build();
        jpaRepository.save(entity);
    }
}
