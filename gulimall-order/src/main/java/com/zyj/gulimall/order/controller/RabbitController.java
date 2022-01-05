package com.zyj.gulimall.order.controller;

import com.zyj.gulimall.order.entity.OrderEntity;
import com.zyj.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @program: gulimall
 * @ClassName RabbitController
 * @author: YaJun
 * @Date: 2022 - 01 - 04 - 21:46
 * @Package: com.zyj.gulimall.order.controller
 * @Description:
 */
@Slf4j
@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMq")
    public String sendMq(@RequestParam(value = "num", defaultValue = "10") Integer num) {
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setCreateTime(new Date());
                entity.setName("哈哈" + i);
                // 1. 发送消息，如果发送的消息是一个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable接口
                String msg = "Hello World";
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", entity, new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java22", orderEntity, new CorrelationData(UUID.randomUUID().toString()));
            }
            log.info("消息发送完成{}");
        }
        return "ok";
    }

}
