package com.foodtech.kitchen.worker.foodtech_worker.application.usecases;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProcessEventUseCaseTest {

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private ProcessEventUseCase useCase;

    @Test
    void processAndPublish_happyPath_publishesEvent() {
        // Arrange
        String type = "ORDER_CREATED";
        String payload = "{\"orderId\":\"123\"}";

        // Act
        useCase.processAndPublish(type, payload);

        // Assert
        ArgumentCaptor<FoodEvent> captor = ArgumentCaptor.forClass(FoodEvent.class);
        verify(eventPublisherPort).publish(captor.capture());

        FoodEvent evt = captor.getValue();
        assertNotNull(evt.getEventId(), "eventId should be generated");
        assertEquals(type, evt.getEventType());
        assertEquals(payload, evt.getPayload());
        assertNotNull(evt.getTimestamp(), "timestamp should be set");
        assertTrue(evt.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void processAndPublish_nullValues_stillPublishesWithGeneratedMeta() {
        // Arrange
        String type = null;
        String payload = null;

        // Act
        useCase.processAndPublish(type, payload);

        // Assert
        ArgumentCaptor<FoodEvent> captor = ArgumentCaptor.forClass(FoodEvent.class);
        verify(eventPublisherPort).publish(captor.capture());

        FoodEvent evt = captor.getValue();
        assertNotNull(evt.getEventId());
        assertNull(evt.getEventType());
        assertNull(evt.getPayload());
        assertNotNull(evt.getTimestamp());
    }
}

