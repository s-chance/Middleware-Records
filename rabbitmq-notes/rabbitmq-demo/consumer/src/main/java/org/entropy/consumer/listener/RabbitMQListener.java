package org.entropy.consumer.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RabbitMQListener {
    @RabbitListener(queues = "simple.queue")
    public void listenSimpleQueueMessage(String msg) {
        System.out.println("consumer receive message from simple.queue [" + msg + "]");
    }

    @RabbitListener(queues = "work.queue")
    public void listenWorkQueue1(String msg) throws InterruptedException {
        System.out.println("consumer 1 receive message from work.queue [" + msg + "]");
        TimeUnit.MILLISECONDS.sleep(20);
    }

    @RabbitListener(queues = "work.queue")
    public void listenSimpleQueue2(String msg) throws InterruptedException {
        System.err.println("consumer 2 receive message from work.queue.... [" + msg + "]");
        TimeUnit.MILLISECONDS.sleep(200);
    }

    @RabbitListener(queues = "fanout.queue1")
    public void listenFanoutQueue1(String msg) {
        System.out.println("consumer receive message from fanout.queue1.... [" + msg + "]");
    }

    @RabbitListener(queues = "fanout.queue2")
    public void listenFanoutQueue2(String msg) {
        System.out.println("consumer receive message from fanout.queue2.... [" + msg + "]");
    }

    @RabbitListener(queues = "direct.queue1")
    public void listenDirectQueue1(String msg) {
        System.out.println("consumer receive message from direct.queue1.... [" + msg + "]");
    }

    @RabbitListener(queues = "direct.queue2")
    public void listenDirectQueue2(String msg) {
        System.out.println("consumer receive message from direct.queue2.... [" + msg + "]");
    }

    @RabbitListener(queues = "topic.queue1")
    public void listenTopicQueue1(String msg) {
        System.out.println("consumer receive message from topic.queue1.... [" + msg + "]");
    }

    @RabbitListener(queues = "topic.queue2")
    public void listenTopicQueue2(String msg) {
        System.out.println("consumer receive message from topic.queue2.... [" + msg + "]");
    }
}
