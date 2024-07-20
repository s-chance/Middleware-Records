package org.entropy.publisher.service;

import java.util.Map;

public interface IPayService {

    Long createOrderId(Map<String, Object> data);

    Boolean payOrder(Long id);

    Boolean getPayStatus(Long id);
}
