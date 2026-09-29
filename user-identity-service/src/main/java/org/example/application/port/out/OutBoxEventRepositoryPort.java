package org.example.application.port.out;

import org.example.domain.entity.Account;
import org.example.infrastructure.persistence.entity.OutBoxEventJpaEntity;

public interface OutBoxEventRepositoryPort {
    void saveEvent(String aggregateType, String aggregateId, String payload,String type);
}
