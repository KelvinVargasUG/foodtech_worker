package com.foodtech.kitchen.worker.foodtech_worker;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
@ConditionalOnProperty(
        name = "foodtech.message-broker",
        havingValue = "kafka",
        matchIfMissing = false
)
public class KafkaConfig {

    /**
     * ReplyingKafkaTemplate para patrón request-reply con Kafka
     * Usa los beans autoconfigurables de Spring Boot
     */
    @Bean
    public ReplyingKafkaTemplate<String, Object, String> replyingKafkaTemplate(
            ProducerFactory<?, ?> producerFactory,
            ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory) {

        ConcurrentMessageListenerContainer<?, ?> replyContainer =
                kafkaListenerContainerFactory.createContainer("factura-response");
        replyContainer.getContainerProperties().setGroupId("worker-reply-group");

        @SuppressWarnings("unchecked")
        ReplyingKafkaTemplate<String, Object, String> template = 
                new ReplyingKafkaTemplate<>(
                    (ProducerFactory<String, Object>) producerFactory, 
                    (ConcurrentMessageListenerContainer<String, String>) replyContainer);
        return template;
    }
}