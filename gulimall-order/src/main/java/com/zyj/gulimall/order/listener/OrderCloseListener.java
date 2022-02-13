package com.zyj.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zyj.gulimall.order.entity.OrderEntity;
import com.zyj.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @program: gulimall
 * @ClassName OrderCloseListener
 * @author: YaJun
 * @Date: 2022 - 01 - 23 - 19:45
 * @Package: com.zyj.gulimall.order.listener
 * @Description: 监听关单队列
 */
@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单信息，准备删除订单：" + orderEntity.getOrderSn());
        try {
            // 判断当前消息是否是第二次及以后（重新）派发过来的
            //Boolean redelivered = message.getMessageProperties().getRedelivered();
            orderService.closeOrder(orderEntity);
            // 手动调用支付宝API进行收单
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
