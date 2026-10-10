package org.example.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxEntity {
    private String id;

    @JsonProperty("aggregate_type")
    @JsonAlias({"aggregateType", "aggregate_type"})
    private String aggregateType;

    @JsonProperty("aggregate_id")
    @JsonAlias({"aggregateId", "aggregate_id"})
    private String aggregateId;

    private String type;

    private String payload;

    @JsonProperty("create_at")
    @JsonAlias({"createAt", "create_at", "createdAt", "created_at"})
    private String createdAt;
}