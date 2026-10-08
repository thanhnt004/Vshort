package org.example.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumEvent<T> {
    private EventPayload<T> payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventPayload<T> {
        private T before;
        private T after;
        private String op; // c: create, u: update, d: delete, r: read
    }
}