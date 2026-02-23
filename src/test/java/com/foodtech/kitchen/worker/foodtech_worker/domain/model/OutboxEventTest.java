package com.foodtech.kitchen.worker.foodtech_worker.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    void builder_ShouldPopulateAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        OutboxEvent event = OutboxEvent.builder()
                .id(id)
                .aggregateType("ORDER")
                .aggregateId("123")
                .eventType("ORDER_CREATED")
                .payload("{\"order\":123}")
                .status("NEW")
                .attempts(0)
                .createdAt(createdAt)
                .build();

        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getAggregateType()).isEqualTo("ORDER");
        assertThat(event.getAggregateId()).isEqualTo("123");
        assertThat(event.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(event.getPayload()).isEqualTo("{\"order\":123}");
        assertThat(event.getStatus()).isEqualTo("NEW");
        assertThat(event.getAttempts()).isEqualTo(0);
        assertThat(event.getCreatedAt()).isEqualTo(createdAt);
    }
}
