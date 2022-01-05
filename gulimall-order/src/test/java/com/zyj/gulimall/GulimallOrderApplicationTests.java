package com.zyj.gulimall;

import com.zyj.gulimall.order.entity.OrderEntity;
import com.zyj.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.UUID;


@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void sendMessageTest() {

        // 2. 发送的对象类型消息为json
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
                entity.setId(1L);
                entity.setCreateTime(new Date());
                entity.setName("哈哈" + i);
                // 1. 发送消息，如果发送的消息是一个对象，我们会使用序列化机制，将对象写出去。对象必须实现Serializable接口
                String msg = "Hello World";
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", entity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", orderEntity);
            }
            log.info("消息发送完成{}");
        }
    }

    /**
     * 1. 如何创建Exchange、Queue、Binding
     * 1.1、使用AmqpAdmin进行创建
     * 2. 如何收发消息
     */
    @Test
    void createExchange() {
        // Exchange
        // public DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }

    @Test
    void createQueue() {
        // public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments)
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    void createBinding() {
        // public Binding(String destination 【目的地】,
        // DestinationType destinationType 【目的地类型】,
        // String exchange 【交换机】,
        // String routingKey 【路由键】,
        // @Nullable Map<String, Object> arguments) 【自定义参数】
        // 将exchange指定的交换机和destination目的地进行绑定，使用routingKey作为指定的路由键
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello-java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }

}
