package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.config;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka.KafkaEventPublisher;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq.RabbitMqEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MessageBrokerConfigTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MessageBrokerConfig config;

    @BeforeEach
    void setUp() {
        // messageBroker value is set per test
    }

    @Test
    void eventPublisherPort_kafkaWithTemplate_returnsKafkaPublisher() {
        // Arrange
        setMessageBroker("kafka");

        // Act
        EventPublisherPort publisher = config.eventPublisherPort(
                Optional.of(kafkaTemplate),
                rabbitTemplate,
                "topic-x",
                "ex",
                "rk");

        // Assert
        assertEquals(KafkaEventPublisher.class, publisher.getClass());
    }

    @Test
    void eventPublisherPort_kafkaWithoutTemplate_fallsBackToRabbitMq() {
        // Arrange
        setMessageBroker("kafka");

        // Act
        EventPublisherPort publisher = config.eventPublisherPort(
                Optional.empty(),
                rabbitTemplate,
                "topic-x",
                "ex",
                "rk");

        // Assert
        assertEquals(RabbitMqEventPublisher.class, publisher.getClass());
    }

    @Test
    void eventPublisherPort_rabbitMq_returnsRabbitPublisher() {
        // Arrange
        setMessageBroker("rabbitmq");

        // Act
        EventPublisherPort publisher = config.eventPublisherPort(
                Optional.of(kafkaTemplate),
                rabbitTemplate,
                "topic-x",
                "ex",
                "rk");

        // Assert
        assertEquals(RabbitMqEventPublisher.class, publisher.getClass());
    }

    @Test
    void eventPublisherPort_unknownBroker_defaultsToRabbitMq() {
        // Arrange
        setMessageBroker("unknown");

        // Act
        EventPublisherPort publisher = config.eventPublisherPort(
                Optional.of(kafkaTemplate),
                rabbitTemplate,
                "topic-x",
                "ex",
                "rk");

        // Assert
        assertEquals(RabbitMqEventPublisher.class, publisher.getClass());
    }

    @Test
    void eventPublisherPort_caseInsensitive_brokerSelection() {
        // Arrange
        setMessageBroker("KaFkA");

        // Act
        EventPublisherPort publisher = config.eventPublisherPort(
                Optional.of(kafkaTemplate),
                rabbitTemplate,
                "topic-x",
                "ex",
                "rk");

        // Assert
        assertTrue(publisher instanceof KafkaEventPublisher);
    }

    private void setMessageBroker(String value) {
        try {
            Field field = MessageBrokerConfig.class.getDeclaredField("messageBroker");
            field.setAccessible(true);
            field.set(config, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to set messageBroker for test", ex);
        }
    }
}
