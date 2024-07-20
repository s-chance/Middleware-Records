package org.entropy.publisher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayService implements IPayService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<Long, Boolean> order = new HashMap<>();

    @Override
    public Long createOrderId(Map<String, Object> data) {
        data.forEach((s, o) -> System.out.println(s + ": " + o));
        System.out.println("幂等性校验...成功");

        Long orderId = Long.valueOf(data.get("orderId").toString());
        order.put(orderId, false);
        System.out.println("要支付的订单号: " + orderId);

        return orderId;
    }

    @Override
    public Boolean payOrder(Long id) {
        log.info("等待支付");
        // 模拟用户等待支付的时间
        try {
            TimeUnit.SECONDS.sleep(30);
            System.out.println("扣减余额...成功");
            log.info("支付成功");
        } catch (InterruptedException e) {
            log.error("系统故障", e);
        }

        // 已支付的消息给交易服务
        try {
            rabbitTemplate.convertAndSend("pay.direct", "pay.success", id);
            log.info("已支付的消息发送成功");
        } catch (AmqpException e) {
            log.error("已支付的消息发送失败", e);
        }
        // 支付服务的订单支付状态为已支付
        order.put(id, true);
        return true;
    }

    @Override
    public Boolean getPayStatus(Long id) {
        return order.get(id);
    }
}
