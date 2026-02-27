package com.foodtech.kitchen.worker.foodtech_worker.application.usecases;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.OutboxRepositoryPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessOutboxUseCase - Complete Unit Tests")
class ProcessOutboxUseCaseCompleteTest {

    @Mock
    private OutboxRepositoryPort outboxRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private ProcessOutboxUseCase processOutboxUseCase;

    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;

    @Captor
    private ArgumentCaptor<FoodEvent> foodEventCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(processOutboxUseCase, "maxAttempts", 3);
    }

    @Test
    @DisplayName("Should process no events when outbox is empty")
    void shouldHandleEmptyOutbox() {
        // Arrange
        when(outboxRepositoryPort.findPendingEvents(anyInt()))
                .thenReturn(Collections.emptyList());

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(outboxRepositoryPort).findPendingEvents(10);
        verify(eventPublisherPort, never()).publish(any());
        verify(outboxRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully process single outbox event")
    void shouldProcessSingleEventSuccessfully() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .aggregateType("ORDER")
                .aggregateId("order-123")
                .eventType("ORDER_CREATED")
                .payload("{\"orderId\":\"123\"}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        doNothing().when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(eventPublisherPort).publish(foodEventCaptor.capture());
        FoodEvent publishedEvent = foodEventCaptor.getValue();
        assertEquals(eventId.toString(), publishedEvent.getEventId());
        assertEquals("ORDER_CREATED", publishedEvent.getEventType());
        assertEquals("{\"orderId\":\"123\"}", publishedEvent.getPayload());

        verify(outboxRepositoryPort).save(outboxEventCaptor.capture());
        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals("SENT", savedEvent.getStatus());
        assertEquals(1, savedEvent.getAttempts());
        assertNotNull(savedEvent.getSentAt());
    }

    @Test
    @DisplayName("Should process multiple events in batch")
    void shouldProcessMultipleEventsInBatch() {
        // Arrange
        OutboxEvent event1 = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("EVENT_1")
                .payload("{\"data\":1}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        OutboxEvent event2 = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("EVENT_2")
                .payload("{\"data\":2}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Arrays.asList(event1, event2));
        doNothing().when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(eventPublisherPort, times(2)).publish(any(FoodEvent.class));
        verify(outboxRepositoryPort, times(2)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should handle publishing failure and increment attempts")
    void shouldHandlePublishingFailure() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .eventType("PAYMENT_FAILED")
                .payload("{\"error\":true}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        
        RuntimeException publishException = new RuntimeException("Connection timeout");
        doThrow(publishException).when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(outboxRepositoryPort).save(outboxEventCaptor.capture());
        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals(1, savedEvent.getAttempts());
        assertEquals("Connection timeout", savedEvent.getLastError());
        assertNotEquals("SENT", savedEvent.getStatus());
        assertNotEquals("FAILED", savedEvent.getStatus());
    }

    @Test
    @DisplayName("Should mark event as FAILED when max attempts reached")
    void shouldMarkEventAsFailedWhenMaxAttemptsReached() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .eventType("CRITICAL_EVENT")
                .payload("{\"important\":true}")
                .status("NEW")
                .attempts(2) // Already 2 attempts, next will be 3 (max)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        
        doThrow(new RuntimeException("Max retries exceeded"))
                .when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(outboxRepositoryPort).save(outboxEventCaptor.capture());
        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals(3, savedEvent.getAttempts());
        assertEquals("FAILED", savedEvent.getStatus());
        assertEquals("Max retries exceeded", savedEvent.getLastError());
    }

    @Test
    @DisplayName("Should not mark as FAILED when below max attempts")
    void shouldNotMarkAsFailedWhenBelowMaxAttempts() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .eventType("RETRY_EVENT")
                .payload("{\"retry\":true}")
                .status("NEW")
                .attempts(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        
        doThrow(new RuntimeException("Temporary failure"))
                .when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(outboxRepositoryPort).save(outboxEventCaptor.capture());
        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals(2, savedEvent.getAttempts());
        assertNotEquals("FAILED", savedEvent.getStatus());
        assertEquals("Temporary failure", savedEvent.getLastError());
    }

    @Test
    @DisplayName("Should handle mixed success and failure in batch")
    void shouldHandleMixedSuccessAndFailureInBatch() {
        // Arrange
        OutboxEvent successEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("SUCCESS_EVENT")
                .payload("{\"success\":true}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        OutboxEvent failureEvent = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("FAILURE_EVENT")
                .payload("{\"fail\":true}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Arrays.asList(successEvent, failureEvent));
        
        doNothing()
                .doThrow(new RuntimeException("Second event failed"))
                .when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(eventPublisherPort, times(2)).publish(any(FoodEvent.class));
        verify(outboxRepositoryPort, times(2)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should handle null payload gracefully")
    void shouldHandleNullPayload() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .eventType("NULL_PAYLOAD_EVENT")
                .payload(null)
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        doNothing().when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(eventPublisherPort).publish(foodEventCaptor.capture());
        FoodEvent publishedEvent = foodEventCaptor.getValue();
        assertNull(publishedEvent.getPayload());
    }

    @Test
    @DisplayName("Should handle empty payload")
    void shouldHandleEmptyPayload() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .eventType("EMPTY_PAYLOAD_EVENT")
                .payload("")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        doNothing().when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(eventPublisherPort).publish(foodEventCaptor.capture());
        FoodEvent publishedEvent = foodEventCaptor.getValue();
        assertEquals("", publishedEvent.getPayload());
    }

    @Test
    @DisplayName("Should use custom maxAttempts configuration")
    void shouldUseCustomMaxAttempts() {
        // Arrange
        ReflectionTestUtils.setField(processOutboxUseCase, "maxAttempts", 5);

        UUID eventId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .id(eventId)
                .eventType("CUSTOM_MAX_EVENT")
                .payload("{\"custom\":true}")
                .status("NEW")
                .attempts(4) // One less than custom max
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10))
                .thenReturn(Collections.singletonList(outboxEvent));
        
        doThrow(new RuntimeException("Still failing"))
                .when(eventPublisherPort).publish(any(FoodEvent.class));

        // Act
        processOutboxUseCase.processOutboxEvents();

        // Assert
        verify(outboxRepositoryPort).save(outboxEventCaptor.capture());
        OutboxEvent savedEvent = outboxEventCaptor.getValue();
        assertEquals(5, savedEvent.getAttempts());
        assertEquals("FAILED", savedEvent.getStatus());
    }
}
