package org.entropy.consumer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class DirectConfiguration {
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("test.direct");
    }

    @Bean
    public Queue directQueue1() {
        return new Queue("direct.queue1");
    }

    @Bean
    public Binding direct1BindingRed(Queue directQueue1, DirectExchange directExchange) {
        return BindingBuilder
                .bind(directQueue1)
                .to(directExchange).with("red");
    }

    @Bean
    public Binding direct1BindingBlue(Queue directQueue1, DirectExchange directExchange) {
        return BindingBuilder
                .bind(directQueue1)
                .to(directExchange).with("blue");
    }

    @Bean
    public Queue directQueue2() {
        return new Queue("direct.queue2");
    }

    @Bean
    public Binding direct2BindingRed(Queue directQueue2, DirectExchange directExchange) {
        return BindingBuilder
                .bind(directQueue2)
                .to(directExchange).with("red");
    }

    @Bean
    public Binding direct2BindingGreen(Queue directQueue2, DirectExchange directExchange) {
        return BindingBuilder
                .bind(directQueue2)
                .to(directExchange).with("green");
    }

}
