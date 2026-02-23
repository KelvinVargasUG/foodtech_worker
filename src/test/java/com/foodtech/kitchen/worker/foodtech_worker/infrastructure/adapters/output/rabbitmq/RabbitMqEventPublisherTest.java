package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq;

import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMqEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void publish_ShouldSendEventToRabbitMq() {
        RabbitMqEventPublisher rabbitMqEventPublisher =
                new RabbitMqEventPublisher(rabbitTemplate, "test.exchange", "test.routingKey");

        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("123")
                .eventType("TEST")
                .payload("data")
                .build();

        // Act
        rabbitMqEventPublisher.publish(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(eq("test.exchange"), eq("test.routingKey"), eq(event));
    }
}
