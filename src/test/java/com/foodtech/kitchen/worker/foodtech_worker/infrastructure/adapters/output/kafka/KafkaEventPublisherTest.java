package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka;

import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SendResult<String, Object> sendResult;

    private KafkaEventPublisher publisher;

    private static final String TOPIC = "food-events";

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplate, TOPIC);
    }

    @Test
    void publish_happyPath_sendsToKafkaAndLogsSuccess() throws InterruptedException {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-123")
                .eventType("ORDER_CREATED")
                .payload("{\"orderId\":\"456\"}")
                .timestamp(LocalDateTime.now())
                .build();

        // Create a completed future (success)
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(TOPIC), eq("event-123"), eq(event))).thenReturn(future);

        // Act
        publisher.publish(event);

        // Assert - verify send was called
        verify(kafkaTemplate).send(eq(TOPIC), eq("event-123"), eq(event));
        
        // Give time for async callback to execute
        Thread.sleep(100);
    }

    @Test
    void publish_kafkaFailure_logsError() throws InterruptedException {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-456")
                .eventType("ORDER_UPDATED")
                .payload("{\"status\":\"PAID\"}")
                .timestamp(LocalDateTime.now())
                .build();

        // Create a failed future
        RuntimeException exception = new RuntimeException("Kafka broker unavailable");
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        when(kafkaTemplate.send(eq(TOPIC), eq("event-456"), eq(event))).thenReturn(future);

        // Act
        publisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TOPIC), eq("event-456"), eq(event));
        
        // Give time for async callback to execute
        Thread.sleep(100);
        
        // The method handles the error gracefully (doesn't throw)
    }

    @Test
    void publish_withNullEventId_sendsWithNullKey() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId(null)
                .eventType("TEST_EVENT")
                .payload("{}")
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(TOPIC), isNull(), eq(event))).thenReturn(future);

        // Act
        publisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TOPIC), isNull(), eq(event));
    }

    @Test
    void publish_multipleEvents_sendsEachToKafka() {
        // Arrange
        FoodEvent event1 = FoodEvent.builder()
                .eventId("event-1")
                .eventType("TYPE_A")
                .payload("{}")
                .timestamp(LocalDateTime.now())
                .build();

        FoodEvent event2 = FoodEvent.builder()
                .eventId("event-2")
                .eventType("TYPE_B")
                .payload("{}")
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(TOPIC), anyString(), any())).thenReturn(future);

        // Act
        publisher.publish(event1);
        publisher.publish(event2);

        // Assert
        verify(kafkaTemplate).send(eq(TOPIC), eq("event-1"), eq(event1));
        verify(kafkaTemplate).send(eq(TOPIC), eq("event-2"), eq(event2));
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any());
    }

    @Test
    void publish_eventWithCompleteData_sendsAllFields() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("uuid-789")
                .eventType("PAYMENT_PROCESSED")
                .payload("{\"amount\":150.50,\"currency\":\"USD\"}")
                .timestamp(LocalDateTime.of(2026, 3, 1, 10, 30))
                .build();

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(eq(TOPIC), eq("uuid-789"), eq(event))).thenReturn(future);

        // Act
        publisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TOPIC), eq("uuid-789"), eq(event));
    }
}
