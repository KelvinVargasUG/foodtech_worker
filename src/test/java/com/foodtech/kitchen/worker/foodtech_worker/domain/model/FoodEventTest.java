package com.foodtech.kitchen.worker.foodtech_worker.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FoodEventTest {

    @Test
    void builder_ShouldPopulateAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();

        FoodEvent event = FoodEvent.builder()
                .eventId("evt-1")
                .eventType("ORDER_CREATED")
                .payload("{\"a\":1}")
                .timestamp(timestamp)
                .build();

        assertThat(event.getEventId()).isEqualTo("evt-1");
        assertThat(event.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(event.getPayload()).isEqualTo("{\"a\":1}");
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
    }
}
