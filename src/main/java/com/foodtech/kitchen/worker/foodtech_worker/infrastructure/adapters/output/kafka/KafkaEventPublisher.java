package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka;

import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.MessageBrokerStrategy;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@RequiredArgsConstructor
public class KafkaEventPublisher implements MessageBrokerStrategy {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${foodtech.kafka.topic:foodtech-events}")
    private String topic;

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

    @Override
    public String getBrokerName() {
        return "kafka";
    }
}
