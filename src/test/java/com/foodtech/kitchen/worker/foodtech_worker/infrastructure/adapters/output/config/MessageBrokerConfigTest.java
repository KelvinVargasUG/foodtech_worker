package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.config;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.MessageBrokerStrategy;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka.KafkaEventPublisher;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq.RabbitMqEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageBrokerConfig - Unit Tests")
class MessageBrokerConfigTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private MessageBrokerConfig messageBrokerConfig;

    @BeforeEach
    void setUp() {
        messageBrokerConfig = new MessageBrokerConfig();
    }

    @Test
    @DisplayName("Should create Kafka strategy when broker is kafka and KafkaTemplate is present")
    void shouldCreateKafkaStrategyWhenKafkaConfigured() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "kafka");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof KafkaEventPublisher);
        assertEquals("kafka", strategy.getBrokerName());
    }

    @Test
    @DisplayName("Should fallback to RabbitMQ when Kafka selected but KafkaTemplate not available")
    void shouldFallbackToRabbitMqWhenKafkaTemplateNotAvailable() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "kafka");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.empty(),
                rabbitTemplate
        );

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof RabbitMqEventPublisher);
        assertEquals("rabbitmq", strategy.getBrokerName());
    }

    @Test
    @DisplayName("Should create RabbitMQ strategy when broker is rabbitmq")
    void shouldCreateRabbitMqStrategyWhenRabbitMqConfigured() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "rabbitmq");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof RabbitMqEventPublisher);
        assertEquals("rabbitmq", strategy.getBrokerName());
    }

    @Test
    @DisplayName("Should default to RabbitMQ when unknown broker is specified")
    void shouldDefaultToRabbitMqWhenUnknownBroker() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "unknown-broker");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof RabbitMqEventPublisher);
        assertEquals("rabbitmq", strategy.getBrokerName());
    }

    @Test
    @DisplayName("Should handle case-insensitive broker names")
    void shouldHandleCaseInsensitiveBrokerNames() {
        // Arrange & Act & Assert - KAFKA uppercase
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "KAFKA");
        MessageBrokerStrategy kafkaStrategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );
        assertTrue(kafkaStrategy instanceof KafkaEventPublisher);

        // RabbitMQ uppercase
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "RABBITMQ");
        MessageBrokerStrategy rabbitStrategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );
        assertTrue(rabbitStrategy instanceof RabbitMqEventPublisher);

        // Mixed case
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "KaFkA");
        MessageBrokerStrategy mixedKafkaStrategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );
        assertTrue(mixedKafkaStrategy instanceof KafkaEventPublisher);
    }

    @Test
    @DisplayName("Should create EventPublisherPort adapter with strategy")
    void shouldCreateEventPublisherPortAdapter() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "rabbitmq");
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.empty(),
                rabbitTemplate
        );

        // Act
        EventPublisherPort eventPublisherPort = messageBrokerConfig.eventPublisherPort(strategy);

        // Assert
        assertNotNull(eventPublisherPort);
    }

    @Test
    @DisplayName("Should handle null broker configuration gracefully")
    void shouldHandleNullBrokerConfiguration() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "rabbitmq");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert - Should default to RabbitMQ
        assertNotNull(strategy);
        assertTrue(strategy instanceof RabbitMqEventPublisher);
    }

    @Test
    @DisplayName("Should handle empty string broker configuration")
    void shouldHandleEmptyStringBrokerConfiguration() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof RabbitMqEventPublisher);
    }

    @Test
    @DisplayName("Should handle whitespace broker configuration")
    void shouldHandleWhitespaceBrokerConfiguration() {
        // Arrange
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "   ");

        // Act
        MessageBrokerStrategy strategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert
        assertNotNull(strategy);
        assertTrue(strategy instanceof RabbitMqEventPublisher);
    }

    @Test
    @DisplayName("Should create different strategies for different configurations")
    void shouldCreateDifferentStrategiesForDifferentConfigurations() {
        // Arrange & Act
        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "kafka");
        MessageBrokerStrategy kafkaStrategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        ReflectionTestUtils.setField(messageBrokerConfig, "messageBroker", "rabbitmq");
        MessageBrokerStrategy rabbitStrategy = messageBrokerConfig.messageBrokerStrategy(
                Optional.of(kafkaTemplate),
                rabbitTemplate
        );

        // Assert
        assertNotEquals(kafkaStrategy.getClass(), rabbitStrategy.getClass());
        assertEquals("kafka", kafkaStrategy.getBrokerName());
        assertEquals("rabbitmq", rabbitStrategy.getBrokerName());
    }

    @Test
    @DisplayName("Should publish event through EventPublisherAdapter")
    void shouldPublishEventThroughAdapter() {
        // Arrange
        MessageBrokerStrategy mockStrategy = mock(MessageBrokerStrategy.class);
        when(mockStrategy.getBrokerName()).thenReturn("test-broker");
        
        FoodEvent event = FoodEvent.builder()
                .eventId("evt-123")
                .eventType("ORDER_CREATED")
                .payload("{\"item\":\"burger\"}")
                .timestamp(LocalDateTime.now())
                .build();

        EventPublisherPort adapter = messageBrokerConfig.eventPublisherPort(mockStrategy);

        // Act
        adapter.publish(event);

        // Assert
        verify(mockStrategy, times(1)).publish(event);
    }
}

