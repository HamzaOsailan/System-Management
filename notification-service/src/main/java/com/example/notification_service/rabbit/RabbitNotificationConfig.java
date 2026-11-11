package com.example.notification_service.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitNotificationConfig {

    public static final String QUEUE = "sahab.notifications.queue";
    public static final String EXCHANGE = "sahab.notifications.exchange";
    public static final String ROUTING_KEY = "notification.created";

    @Bean
    public Queue sahabNotificationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public DirectExchange sahabNotificationExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Binding sahabNotificationBinding(
            Queue sahabNotificationQueue,
            DirectExchange sahabNotificationExchange) {
        return BindingBuilder
                .bind(sahabNotificationQueue)
                .to(sahabNotificationExchange)
                .with(ROUTING_KEY);
    }
}
