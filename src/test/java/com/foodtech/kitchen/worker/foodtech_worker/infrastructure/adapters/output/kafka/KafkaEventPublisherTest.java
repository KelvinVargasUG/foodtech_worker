package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.kafka;

import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaEventPublisher - Unit Tests")
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private CompletableFuture<SendResult<String, Object>> sendResultFuture;

    @Captor
    private ArgumentCaptor<BiConsumer<SendResult<String, Object>, Throwable>> callbackCaptor;

    private KafkaEventPublisher kafkaEventPublisher;

    private static final String TEST_TOPIC = "test-topic";

    @BeforeEach
    void setUp() {
        kafkaEventPublisher = new KafkaEventPublisher(kafkaTemplate);
        ReflectionTestUtils.setField(kafkaEventPublisher, "topic", TEST_TOPIC);
    }

    @Test
    @DisplayName("Should publish event successfully to Kafka topic")
    void shouldPublishEventSuccessfully() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-123")
                .eventType("ORDER_CREATED")
                .payload("{\"orderId\":\"123\"}")
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(sendResultFuture);
        when(sendResultFuture.whenComplete(any())).thenReturn(sendResultFuture);

        // Act
        kafkaEventPublisher.publish(event);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq(TEST_TOPIC), eq("event-123"), eq(event));
        verify(sendResultFuture, times(1)).whenComplete(any());
    }

    @Test
    @DisplayName("Should handle successful callback when event is published")
    void shouldHandleSuccessfulCallback() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-456")
                .eventType("PAYMENT_PROCESSED")
                .payload("{\"amount\":100}")
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(sendResultFuture);
        
        doAnswer(invocation -> {
            BiConsumer<SendResult<String, Object>, Throwable> callback = invocation.getArgument(0);
            callback.accept(mock(SendResult.class), null); // Simulate success
            return sendResultFuture;
        }).when(sendResultFuture).whenComplete(any());

        // Act
        kafkaEventPublisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TEST_TOPIC), eq("event-456"), eq(event));
        verify(sendResultFuture).whenComplete(callbackCaptor.capture());
        
        // Verify callback was invoked with success
        BiConsumer<SendResult<String, Object>, Throwable> capturedCallback = callbackCaptor.getValue();
        assertNotNull(capturedCallback);
    }

    @Test
    @DisplayName("Should handle error callback when event publishing fails")
    void shouldHandleErrorCallback() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-789")
                .eventType("REFUND_REQUESTED")
                .payload("{\"refundId\":\"ref-001\"}")
                .timestamp(LocalDateTime.now())
                .build();

        Exception testException = new RuntimeException("Kafka connection failed");

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(sendResultFuture);
        
        doAnswer(invocation -> {
            BiConsumer<SendResult<String, Object>, Throwable> callback = invocation.getArgument(0);
            callback.accept(null, testException); // Simulate failure
            return sendResultFuture;
        }).when(sendResultFuture).whenComplete(any());

        // Act
        kafkaEventPublisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TEST_TOPIC), eq("event-789"), eq(event));
        verify(sendResultFuture).whenComplete(callbackCaptor.capture());
        
        // Verify callback handles error properly
        BiConsumer<SendResult<String, Object>, Throwable> capturedCallback = callbackCaptor.getValue();
        assertNotNull(capturedCallback);
    }

    @Test
    @DisplayName("Should return correct broker name")
    void shouldReturnKafkaBrokerName() {
        // Arrange & Act
        String brokerName = kafkaEventPublisher.getBrokerName();

        // Assert
        assertEquals("kafka", brokerName);
    }

    @Test
    @DisplayName("Should publish event with null payload")
    void shouldPublishEventWithNullPayload() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-null")
                .eventType("NULL_TEST")
                .payload(null)
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(sendResultFuture);
        when(sendResultFuture.whenComplete(any())).thenReturn(sendResultFuture);

        // Act
        kafkaEventPublisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TEST_TOPIC), eq("event-null"), eq(event));
    }

    @Test
    @DisplayName("Should publish event with empty payload")
    void shouldPublishEventWithEmptyPayload() {
        // Arrange
        FoodEvent event = FoodEvent.builder()
                .eventId("event-empty")
                .eventType("EMPTY_TEST")
                .payload("")
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(sendResultFuture);
        when(sendResultFuture.whenComplete(any())).thenReturn(sendResultFuture);

        // Act
        kafkaEventPublisher.publish(event);

        // Assert
        verify(kafkaTemplate).send(eq(TEST_TOPIC), eq("event-empty"), eq(event));
    }

    @Test
    @DisplayName("Should handle multiple events in sequence")
    void shouldHandleMultipleEventsInSequence() {
        // Arrange
        FoodEvent event1 = FoodEvent.builder()
                .eventId("event-1")
                .eventType("TYPE_1")
                .payload("{\"data\":1}")
                .timestamp(LocalDateTime.now())
                .build();

        FoodEvent event2 = FoodEvent.builder()
                .eventId("event-2")
                .eventType("TYPE_2")
                .payload("{\"data\":2}")
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(sendResultFuture);
        when(sendResultFuture.whenComplete(any())).thenReturn(sendResultFuture);

        // Act
        kafkaEventPublisher.publish(event1);
        kafkaEventPublisher.publish(event2);

        // Assert
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any());
        verify(kafkaTemplate).send(eq(TEST_TOPIC), eq("event-1"), eq(event1));
        verify(kafkaTemplate).send(eq(TEST_TOPIC), eq("event-2"), eq(event2));
    }
}
