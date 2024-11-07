package com.hmall.trade.listener;

import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PayStatusListener {
    @Resource
    IOrderService orderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("trade.pay.success.queue"),
            exchange = @Exchange("pay.direct"),
            key = "pay.success"
    ))
    public void listenPaySuccess(Long orderId) {
        // 1.查询订单
        Order order = orderService.getById(orderId);
        // 2.判断订单状态，是否为未支付
        if (order == null || order.getStatus() != 1) {
            // 不作处理
            return;
        }
        // 3.标记订单状态为已支付
        orderService.markOrderPaySuccess(orderId);
    }
}
