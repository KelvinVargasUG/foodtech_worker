package com.foodtech.kitchen.worker.foodtech_worker.application.usecases;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.OutboxRepositoryPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.OutboxEvent;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessOutboxUseCaseTest {

    @Mock
    private OutboxRepositoryPort outboxRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    // Will construct useCase explicitly in setup

    private ProcessOutboxUseCase useCase;

    @BeforeEach
    void setUp() {
        // instantiate with mocks and a controlled maxAttempts
        useCase = new ProcessOutboxUseCase(outboxRepositoryPort, eventPublisherPort, 3);
    }

    @Test
    void processOutboxEvents_noPending_doesNothing() {
        // Arrange
        when(outboxRepositoryPort.findPendingEvents(10)).thenReturn(Collections.emptyList());

        // Act
        useCase.processOutboxEvents();

        // Assert
        verify(outboxRepositoryPort).findPendingEvents(10);
        verifyNoMoreInteractions(outboxRepositoryPort);
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void processOutboxEvents_successfulPublish_marksSentAndSaves() {
        // Arrange
        OutboxEvent outbox = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("ORDER_CREATED")
                .payload("{\"id\":\"1\"}")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10)).thenReturn(List.of(outbox));

        // Act
        useCase.processOutboxEvents();

        // Assert
        ArgumentCaptor<FoodEvent> publishCaptor = ArgumentCaptor.forClass(FoodEvent.class);
        verify(eventPublisherPort).publish(publishCaptor.capture());

        FoodEvent published = publishCaptor.getValue();
        assertEquals(outbox.getId().toString(), published.getEventId());
        assertEquals(outbox.getEventType(), published.getEventType());
        assertEquals(outbox.getPayload(), published.getPayload());

        ArgumentCaptor<OutboxEvent> saveCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepositoryPort, times(1)).save(saveCaptor.capture());

        OutboxEvent saved = saveCaptor.getValue();
        assertEquals("SENT", saved.getStatus());
        assertNotNull(saved.getSentAt());
        assertEquals(1, saved.getAttempts());
    }

    @Test
    void processOutboxEvents_publishThrows_incrementsAttempts_andRetriesLater() {
        // Arrange
        OutboxEvent outbox = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("ORDER_UPDATED")
                .payload("{}")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("boom")).when(eventPublisherPort).publish(any());

        // Act
        useCase.processOutboxEvents();

        // Assert
        ArgumentCaptor<OutboxEvent> saveCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepositoryPort).save(saveCaptor.capture());

        OutboxEvent saved = saveCaptor.getValue();
        assertEquals(1, saved.getAttempts());
        assertEquals("boom", saved.getLastError());
        assertNotEquals("FAILED", saved.getStatus());
    }

    @Test
    void processOutboxEvents_publishThrows_reachesMaxAttempts_marksFailed() {
        // Arrange
        OutboxEvent outbox = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("ORDER_UPDATED")
                .payload("{}")
                .attempts(2) // one less than max(3)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10)).thenReturn(List.of(outbox));
        doThrow(new RuntimeException("fatal")).when(eventPublisherPort).publish(any());

        // Act
        useCase.processOutboxEvents();

        // Assert
        ArgumentCaptor<OutboxEvent> saveCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepositoryPort).save(saveCaptor.capture());

        OutboxEvent saved = saveCaptor.getValue();
        assertEquals(3, saved.getAttempts());
        assertEquals("fatal", saved.getLastError());
        assertEquals("FAILED", saved.getStatus());
    }
}

