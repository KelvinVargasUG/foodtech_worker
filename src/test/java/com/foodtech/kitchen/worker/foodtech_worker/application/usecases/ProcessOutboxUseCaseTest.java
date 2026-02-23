package com.foodtech.kitchen.worker.foodtech_worker.application.usecases;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.OutboxRepositoryPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessOutboxUseCaseTest {

    @Mock
    private OutboxRepositoryPort outboxRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private ProcessOutboxUseCase processOutboxUseCase;

    @BeforeEach
    void setUp() {
        processOutboxUseCase = new ProcessOutboxUseCase(outboxRepositoryPort, eventPublisherPort, 3);
    }

    @Test
    void processOutboxEvents_ShouldMarkAsSent_WhenPublishSucceeds() {
        OutboxEvent pending = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("ORDER_CREATED")
                .payload("{\"item\":\"Burger\"}")
                .status("NEW")
                .attempts(0)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10)).thenReturn(List.of(pending));

        processOutboxUseCase.processOutboxEvents();

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepositoryPort).save(outboxCaptor.capture());

        OutboxEvent saved = outboxCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("SENT");
        assertThat(saved.getAttempts()).isEqualTo(1);
        assertThat(saved.getSentAt()).isNotNull();
    }

    @Test
    void processOutboxEvents_ShouldMarkAsFailed_WhenMaxAttemptsReached() {
        OutboxEvent pending = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventType("ORDER_CREATED")
                .payload("{\"item\":\"Burger\"}")
                .status("FAILED")
                .attempts(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(outboxRepositoryPort.findPendingEvents(10)).thenReturn(List.of(pending));
        doThrow(new RuntimeException("broker down")).when(eventPublisherPort).publish(any());

        processOutboxUseCase.processOutboxEvents();

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepositoryPort).save(outboxCaptor.capture());

        OutboxEvent saved = outboxCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("FAILED");
        assertThat(saved.getAttempts()).isEqualTo(3);
        assertThat(saved.getLastError()).contains("broker down");
    }
}
