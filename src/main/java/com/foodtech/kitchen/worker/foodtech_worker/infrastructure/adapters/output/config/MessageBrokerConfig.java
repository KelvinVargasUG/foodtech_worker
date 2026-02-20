package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.config;

import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.MessageBrokerStrategy;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq.RabbitMqEventPublisher;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka.KafkaEventPublisher;
import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.Optional;

@Slf4j
@Configuration
public class MessageBrokerConfig {

    @Value("${foodtech.message-broker:rabbitmq}")
    private String messageBroker;

    /**
     * Factory method to select the appropriate message broker strategy
     * based on the application configuration.
     *
     * @param kafkaTemplate Kafka template (optional, only present if Kafka is enabled)
     * @param rabbitTemplate RabbitMQ template
     * @return The selected message broker strategy
     */
    @Bean
    public MessageBrokerStrategy messageBrokerStrategy(
            Optional<KafkaTemplate<String, Object>> kafkaTemplate,
            RabbitTemplate rabbitTemplate) {
        
        MessageBrokerStrategy strategy;
        
        switch (messageBroker.toLowerCase()) {
            case "kafka":
                log.info("Using Kafka as message broker");
                if (kafkaTemplate.isPresent()) {
                    strategy = new KafkaEventPublisher(kafkaTemplate.get());
                } else {
                    log.warn("Kafka selected but KafkaTemplate not available. Falling back to RabbitMQ");
                    strategy = new RabbitMqEventPublisher(rabbitTemplate);
                }
                break;
            case "rabbitmq":
                log.info("Using RabbitMQ as message broker");
                strategy = new RabbitMqEventPublisher(rabbitTemplate);
                break;
            default:
                log.warn("Unknown message broker: {}. Defaulting to RabbitMQ", messageBroker);
                strategy = new RabbitMqEventPublisher(rabbitTemplate);
        }
        
        log.info("Message broker strategy initialized: {}", strategy.getBrokerName());
        return strategy;
    }

    /**
     * EventPublisherPort adapter that delegates to the selected strategy
     *
     * @param strategy The message broker strategy
     * @return EventPublisherPort implementation
     */
    @Bean
    public EventPublisherPort eventPublisherPort(MessageBrokerStrategy strategy) {
        return new EventPublisherAdapter(strategy);
    }

    /**
     * Adapter class that implements EventPublisherPort using the strategy pattern
     */
    @Slf4j
    private static class EventPublisherAdapter implements EventPublisherPort {
        
        private final MessageBrokerStrategy strategy;

        public EventPublisherAdapter(MessageBrokerStrategy strategy) {
            this.strategy = strategy;
        }

        @Override
        public void publish(com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent event) {
            log.debug("Publishing event using strategy: {}", strategy.getBrokerName());
            strategy.publish(event);
        }
    }
}
