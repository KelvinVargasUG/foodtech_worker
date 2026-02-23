package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq;

import com.foodtech.kitchen.worker.foodtech_worker.application.ports.output.EventPublisherPort;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Slf4j
@RequiredArgsConstructor
public class RabbitMqEventPublisher implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    @Override
    public void publish(FoodEvent event) {
        log.info("Publishing event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Event sent to exchange: {}, routingKey: {}", exchange, routingKey);
    }
}
