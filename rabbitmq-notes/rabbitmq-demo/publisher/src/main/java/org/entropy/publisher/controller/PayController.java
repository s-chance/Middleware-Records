package org.entropy.publisher.controller;

import lombok.RequiredArgsConstructor;
import org.entropy.publisher.service.IPayService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PayController {

    private final IPayService payService;

    @PostMapping("/payOrder")
    public String createPayOrder(@RequestBody Map<String, Object> data) {
        Long orderId = payService.createOrderId(data);
        return "成功生成支付单，支付的订单号为 " + orderId;
    }

    @PostMapping("/pay/{orderId}")
    public Boolean payOrder(@PathVariable("orderId") Long id) {
        return payService.payOrder(id);
    }

    @GetMapping("/pay/status/{orderId}")
    public Boolean payStatus(@PathVariable("orderId") Long id) {
        return payService.getPayStatus(id);
    }
}
