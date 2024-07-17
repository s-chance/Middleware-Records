package org.entropy.publisher;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class PublisherApplicationTests {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSimpleQueue() {
        String queueName = "simple.queue";
        String message = "hello, spring amqp!";
        rabbitTemplate.convertAndSend(queueName, message);
    }

    @Test
    void testWorkQueue() throws InterruptedException {
        for (int i = 1; i <= 50; i++) {
            String queueName = "work.queue";
            String message = "hello, worker, message_" + i;
            rabbitTemplate.convertAndSend(queueName, message);
            TimeUnit.MILLISECONDS.sleep(20);
        }
    }

    @Test
    void testFanout() {
        String exchangeName = "test.fanout";
        String msg = "this is a fanout message";
        rabbitTemplate.convertAndSend(exchangeName, "", msg);
    }

    @Test
    void testDirect() {
        String exchangeName = "test.direct";
        String rk = "blue";
        String msg = "this is a direct message from " + rk;
        rabbitTemplate.convertAndSend(exchangeName, rk, msg);
    }

    @Test
    void testTopic() {
        String exchangeName = "test.topic";
        String rk = "china.news";
        String msg = "this is a topic message about " + rk;
        rabbitTemplate.convertAndSend(exchangeName, rk, msg);
    }

    @Test
    void testObject() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("name", "tom");
        msg.put("age", 22);
        rabbitTemplate.convertAndSend("object.queue", msg);
    }

    @Test
    void testConfirmCallback() {
        rabbitTemplate.convertAndSend("test.direct","red", "hello");
    }

    @Test
    void testPageOut() {
        Message message = MessageBuilder
                .withBody("hello".getBytes(StandardCharsets.UTF_8))
                .setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT) // PERSISTENT
                .build();
        for (int i = 0; i < 1000_000; i++) {
            rabbitTemplate.convertAndSend("simple.queue", message);
        }
    }
}
