package org.entropy.common.constant;

public interface MqConstant {
    String DELAY_EXCHANGE = "trade.delay.topic";
    String DELAY_ORDER_QUEUE = "trade.order.delay.queue";
    String DELAY_ORDER_ROUTING_KEY = "order.query";

    Long[] DELAY_MILLIS = new Long[]{10000L, 10000L, 10000L, 15000L, 15000L, 30000L, 30000L};
}
