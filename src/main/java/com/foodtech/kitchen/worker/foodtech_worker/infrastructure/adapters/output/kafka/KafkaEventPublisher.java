package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    @Override
    public void publish(FoodEvent event) {
        log.info("Publishing event to Kafka topic '{}': {}", topic, event);
        kafkaTemplate.send(topic, event.getEventId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event published successfully to Kafka topic: {} with message key: {}",
                                topic, event.getEventId());
                    } else {
                        log.error("Failed to publish event to Kafka topic: {}", topic, ex);
                    }
                });
    }
}
