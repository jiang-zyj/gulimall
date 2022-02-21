package com.zyj.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zyj.common.to.mq.SecKillOrderTo;
import com.zyj.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @program: gulimall
 * @ClassName OrderSecKillListener
 * @author: YaJun
 * @Date: 2022 - 02 - 20 - 22:59
 * @Package: com.zyj.gulimall.order.listener
 * @Description:
 */
@Slf4j
@RabbitListener(queues = "order.seckill.order.queue")
@Component
public class OrderSecKillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(SecKillOrderTo secKillOrder, Channel channel, Message message) throws IOException {
        log.info("准备创建秒杀单的详细信息...");

        try {
            orderService.createSecKillOrder(secKillOrder);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
