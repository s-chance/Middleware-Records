package org.entropy.publisher.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MQConfirmConfig implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        // 配置回调
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                log.debug("路由失败，收到消息的 return callback，exchange: {}, routingKey: {}, message: {}, replyCode: {}, replyText: {}",
                        returned.getExchange(), returned.getRoutingKey(), returned.getMessage(),
                        returned.getReplyCode(), returned.getReplyText());
            }
        });

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                if (ack) {
                    // 消息发送成功
                    log.debug("消息发送成功，ack");
                } else {
                    // 消息发送失败
                    log.debug("消息回调失败，nack cause: {}", cause);
                }
            }
        });
    }
}
