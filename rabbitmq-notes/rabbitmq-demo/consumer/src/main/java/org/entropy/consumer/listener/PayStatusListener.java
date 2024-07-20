package org.entropy.consumer.listener;

import lombok.RequiredArgsConstructor;
import org.entropy.consumer.service.IOrderService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PayStatusListener {

    private final IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "pay.queue", durable = "true"),
            exchange = @Exchange(value = "pay.direct"),
            key = "pay.success"
    ))
    public void listenPayQueue(Long orderId) {
        // 模拟网络波动和远程调用带来的通信耗时
        try {
            TimeUnit.SECONDS.sleep(45);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 将交易服务的订单标记为已支付
        orderService.markAsPaid(orderId);
    }
}
