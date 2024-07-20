package org.entropy.consumer.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.entropy.common.config.DelayMessageProcessor;
import org.entropy.common.constant.MqConstant;
import org.entropy.common.domain.MultiDelayMessage;
import org.entropy.consumer.service.IOrderService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private final IOrderService orderService;

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstant.DELAY_ORDER_QUEUE, durable = "true"),
            exchange = @Exchange(value = MqConstant.DELAY_EXCHANGE, delayed = "true", type = ExchangeTypes.TOPIC),
            key = MqConstant.DELAY_ORDER_ROUTING_KEY
    ))
    public void listenOrderDelayQueue(MultiDelayMessage<Long> delayMessage) {
        log.info("收到延迟消息");

        // 在交易服务查询订单支付状态
        Long orderId = delayMessage.getData();
        Boolean payStatus = orderService.getPayStatusById(orderId);
        // 判断是否已支付
        if (payStatus == null || payStatus) {
            // 订单不存在或已支付
            return;
        }

        // 订单状态为未支付，主动查询支付服务
        // 主动查询订单支付状态，是为了避免MQ异步消息通知不同步导致的数据一致性问题
        // 例如，订单已支付，但是MQ异步消息还未及时通知远程服务更新状态

        // 模拟查询远程服务订单支付状态
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://127.0.0.1:8080/pay/status/" + orderId;
        ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);

        // 查询支付状态
        if (response.getStatusCode() == HttpStatus.OK) {
            payStatus = response.getBody();
            if (payStatus == null) {
                payStatus = false;
            }
        } else {
            System.out.println("远程调用异常");
            payStatus = false;
        }


        // 已支付，标记交易服务订单支付状态为已支付
        // 后续不再发送延迟消息
        if (payStatus) {
            orderService.markAsPaid(orderId);
            return;
        }

        // 未支付，尝试获取下次订单延迟时间
        if (delayMessage.hasNextDelay()) {
            // 存在下一个延迟时间，重发延迟消息
            log.info("重发延迟消息");
            Long nextDelay = delayMessage.removeNextDelay();
            rabbitTemplate.convertAndSend(MqConstant.DELAY_EXCHANGE, MqConstant.DELAY_ORDER_ROUTING_KEY,
                    delayMessage, new DelayMessageProcessor(nextDelay));
            return;
        }
        // 不存在下一个延迟时间，取消订单
        orderService.removeOrder(orderId);
        System.out.println("恢复库存...成功");
    }
}
