package org.entropy.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entropy.common.config.DelayMessageProcessor;
import org.entropy.common.constant.MqConstant;
import org.entropy.common.domain.MultiDelayMessage;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final RabbitTemplate rabbitTemplate;

    private final Map<Long, Boolean> order = new HashMap<>();

    @Override
    public Long createOrder(Map<String, Object> orderData) {
        orderData.forEach((s, o) -> System.out.println(s + ": " + o));
        Long orderId = Long.valueOf(orderData.get("orderId").toString());
        // 创建订单，初始支付状态为未支付，默认为false
        order.put(orderId, false);
        System.out.println("查询商品...成功");
        System.out.println("计算价格...成功");
        System.out.println("扣减库存...成功");
        System.out.println("清理购物车...成功");

        // 发送延迟消息用于确认该订单号是否已支付
        try {
            MultiDelayMessage<Long> delayMessage = MultiDelayMessage.of(orderId, MqConstant.DELAY_MILLIS);
            rabbitTemplate.convertAndSend(
                    MqConstant.DELAY_EXCHANGE, MqConstant.DELAY_ORDER_ROUTING_KEY, delayMessage,
                    new DelayMessageProcessor(delayMessage.removeNextDelay())
            );
            log.info("延迟消息发送成功");
        } catch (AmqpException e) {
            log.error("延迟消息发送异常", e);
        }

        // 等待支付服务支付此订单号
        return orderId;
    }

    @Override
    public void markAsPaid(Long id) {
        log.info("订单被标记为已支付");
        order.put(id, true);
    }

    @Override
    public Boolean getPayStatusById(Long id) {
        // 获取订单支付状态
        return order.get(id);
    }

    @Override
    public void removeOrder(Long id) {
        // 取消订单
        log.info("取消订单: " + id);
        order.remove(id);
    }
}
