package org.entropy.consumer.controller;

import lombok.RequiredArgsConstructor;
import org.entropy.consumer.service.IOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @PostMapping("/order")
    public Long createOrder(@RequestBody Map<String, Object> orderData) {
        return orderService.createOrder(orderData);
    }

    @PutMapping("/order/{orderId}")
    public void markAsPaid(@PathVariable("orderId") Long id) {
        orderService.markAsPaid(id);
    }
}
