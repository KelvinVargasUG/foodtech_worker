package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output;

import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;

/**
 * Strategy pattern interface for message broker implementations.
 * This allows switching between different message brokers (RabbitMQ, Kafka, etc.)
 * at runtime based on configuration.
 */
public interface MessageBrokerStrategy {
    
    /**
     * Publish an event to the message broker
     *
     * @param event the event to publish
     */
    void publish(FoodEvent event);
    
    /**
     * Get the name of the message broker strategy
     *
     * @return the name of the broker (e.g., "rabbitmq", "kafka")
     */
    String getBrokerName();
}
