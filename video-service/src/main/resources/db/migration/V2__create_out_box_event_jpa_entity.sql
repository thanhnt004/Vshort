CREATE TABLE out_box_event_jpa_entity (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255),
    aggregate_id VARCHAR(255),
    type VARCHAR(255),
    payload TEXT,
    create_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_out_box_event_aggregate
    ON out_box_event_jpa_entity(aggregate_type, aggregate_id);
