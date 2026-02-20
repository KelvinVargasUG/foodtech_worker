package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Kafka Configuration for Event Publishing
 * This configuration is loaded when foodtech.message-broker=kafka
 * 
 * Uses Spring Boot's auto-configured ProducerFactory and KafkaTemplate
 * to avoid type conflicts and bean creation issues.
 */
@Configuration
@ConditionalOnProperty(
        name = "foodtech.message-broker",
        havingValue = "kafka",
        matchIfMissing = false
)
public class KafkaProducerConfig {

    /**
     * KafkaTemplate para enviar eventos FoodEvent
     * Reutiliza el ProducerFactory autoconfigurando de Spring Boot
     *
     * @param producerFactory Bean autoconfigurable de Spring Boot
     * @return configured KafkaTemplate
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}

