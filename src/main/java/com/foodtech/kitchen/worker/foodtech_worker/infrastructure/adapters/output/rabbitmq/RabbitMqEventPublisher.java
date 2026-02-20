package com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.rabbitmq;

import com.foodtech.kitchen.worker.foodtech_worker.infrastructure.adapters.output.MessageBrokerStrategy;
import com.foodtech.kitchen.worker.foodtech_worker.domain.model.FoodEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@RequiredArgsConstructor
public class RabbitMqEventPublisher implements MessageBrokerStrategy {

    private final RabbitTemplate rabbitTemplate;

    @Value("${foodtech.rabbitmq.exchange}")
    private String exchange;

    @Value("${foodtech.rabbitmq.routingkey}")
    private String routingKey;

    @Override
    public void publish(FoodEvent event) {
        log.info("Publishing event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
        log.info("Event sent to exchange: {}, routingKey: {}", exchange, routingKey);
    }

    @Override
    public String getBrokerName() {
        return "rabbitmq";
    }
}
