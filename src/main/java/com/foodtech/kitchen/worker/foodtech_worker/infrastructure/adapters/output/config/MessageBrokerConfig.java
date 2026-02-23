package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.config;

import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq.RabbitMqEventPublisher;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka.KafkaEventPublisher;
import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
     * Factory method to select the appropriate message broker adapter
     * based on the application configuration.
     *
     * @param kafkaTemplate Kafka template (optional, only present if Kafka is enabled)
     * @param rabbitTemplate RabbitMQ template
     * @return The selected event publisher port implementation
     */
    @Bean
    public EventPublisherPort eventPublisherPort(
            Optional<KafkaTemplate<String, Object>> kafkaTemplate,
            RabbitTemplate rabbitTemplate,
            @Value("${foodtech.kafka.topic:foodtech-events}") String topic,
            @Value("${foodtech.rabbitmq.exchange}") String exchange,
            @Value("${foodtech.rabbitmq.routingkey}") String routingKey) {
        
        EventPublisherPort publisher;
        
        switch (messageBroker.toLowerCase()) {
            case "kafka":
                log.info("Using Kafka as message broker");
                if (kafkaTemplate.isPresent()) {
                    publisher = new KafkaEventPublisher(kafkaTemplate.get(), topic);
                } else {
                    log.warn("Kafka selected but KafkaTemplate not available. Falling back to RabbitMQ");
                    publisher = new RabbitMqEventPublisher(rabbitTemplate, exchange, routingKey);
                }
                break;
            case "rabbitmq":
                log.info("Using RabbitMQ as message broker");
                publisher = new RabbitMqEventPublisher(rabbitTemplate, exchange, routingKey);
                break;
            default:
                log.warn("Unknown message broker: {}. Defaulting to RabbitMQ", messageBroker);
                publisher = new RabbitMqEventPublisher(rabbitTemplate, exchange, routingKey);
        }
        
        log.info("Message broker adapter initialized: {}", publisher.getClass().getSimpleName());
        return publisher;
    }
}
