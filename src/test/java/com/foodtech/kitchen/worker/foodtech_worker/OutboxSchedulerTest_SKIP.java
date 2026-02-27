package com.foodtech.kitchen.worker.foodtech_worker;

import com.foodtech.kitchen.worker.foodtech_worker.application.usecases.ProcessOutboxUseCase;
import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.input.scheduler.OutboxScheduler;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OutboxSchedulerLoggingTest {

    @Test
    void shouldProcessOutboxEventsWhenSchedulerRuns() {
        // Arrange
        ProcessOutboxUseCase useCase = mock(ProcessOutboxUseCase.class);
        OutboxScheduler scheduler = new OutboxScheduler(useCase);

        // Act
        scheduler.processOutbox();

        // Assert
        verify(useCase, times(1)).processOutboxEvents();
    }
}
