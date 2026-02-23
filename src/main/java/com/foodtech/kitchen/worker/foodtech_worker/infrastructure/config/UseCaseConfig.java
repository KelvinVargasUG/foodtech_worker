package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.config;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.OutboxRepositoryPort;
import com.foodtech.kitchen.worker.foodtech_worker.application.usecases.ProcessEventUseCase;
import com.foodtech.kitchen.worker.foodtech_worker.application.usecases.ProcessOutboxUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public ProcessEventUseCase processEventUseCase(EventPublisherPort eventPublisherPort) {
        return new ProcessEventUseCase(eventPublisherPort);
    }

    @Bean
    public ProcessOutboxUseCase processOutboxUseCase(
            OutboxRepositoryPort outboxRepositoryPort,
            EventPublisherPort eventPublisherPort,
            @Value("${foodtech.outbox.max-attempts:3}") int maxAttempts) {
        return new ProcessOutboxUseCase(outboxRepositoryPort, eventPublisherPort, maxAttempts);
    }
}
