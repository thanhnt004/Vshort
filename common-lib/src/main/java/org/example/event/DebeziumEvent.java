package org.example.event;


import lombok.Data;

@Data
public class DebeziumEvent<T> {
    private EventPayload<T> payload;
    @Data
    public static class EventPayload<T> {
        private T before;
        private T after;
        private String op; // c: create, u: update, d: delete, r: read
    }
}