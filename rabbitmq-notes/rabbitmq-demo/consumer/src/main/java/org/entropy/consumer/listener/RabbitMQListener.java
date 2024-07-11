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
}
