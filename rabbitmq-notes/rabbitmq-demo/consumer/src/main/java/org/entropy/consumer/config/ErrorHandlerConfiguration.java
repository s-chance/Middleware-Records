package org.entropy.consumer.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry", name = "enabled", havingValue = "true")
public class ErrorHandlerConfiguration {

    @Bean(name = "ee")
    public DirectExchange errorExchange() {
        return ExchangeBuilder.directExchange("error.direct").build();
    }

    @Bean(name = "eq")
    public Queue errorQueue() {
        return QueueBuilder.durable("error.queue").build();
    }

    @Bean
    public Binding errorBinding(@Qualifier("eq") Queue queue, @Qualifier("ee") DirectExchange directExchange) {
        return BindingBuilder
                .bind(queue)
                .to(directExchange)
                .with("error");
    }

    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
    }
}
