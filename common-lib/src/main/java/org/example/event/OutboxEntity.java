package org.example.event;


import lombok.Data;

@Data
public class OutboxEntity {
    private String id;

    private String aggregateType;

    private String aggregateId;

    private String type;

    private String payload;

    private String createdAt;
}