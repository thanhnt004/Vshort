package org.example.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutBoxEventJpaEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private String aggregateType;
    private String aggregateId;
    private String type;
    private String payload;
    @CreationTimestamp
    private Instant createAt;
}
