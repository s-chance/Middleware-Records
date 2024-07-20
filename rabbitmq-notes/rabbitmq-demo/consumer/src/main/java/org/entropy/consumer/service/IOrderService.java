package org.entropy.consumer.service;

import java.util.Map;

public interface IOrderService {
    Long createOrder(Map<String, Object> orderData);

    void markAsPaid(Long id);

    Boolean getPayStatusById(Long id);

    void removeOrder(Long id);
}
