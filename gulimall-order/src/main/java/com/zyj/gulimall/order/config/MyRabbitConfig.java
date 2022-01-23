package com.zyj.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @program: gulimall
 * @ClassName MyRabbitConfig
 * @author: YaJun
 * @Date: 2022 - 01 - 04 - 21:16
 * @Package: com.zyj.gulimall.order.config
 * @Description: RabbitMQ的自定义配置类
 */
@Configuration
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 给容器中放一个JSON的消息转换器，让SpringBoot使用我们自定义的MessageConverter
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 发送端：
     * 1. 服务器收到消息就回调
     *      1. spring.rabbitmq.publisher-confirm-type=correlated
     *      2. 设置确认回调ConfirmCallback
     * 2. 消息正确抵达队列进行回调
     *      1.  spring.rabbitmq.publisher-returns=true
     *      spring.rabbitmq.template.mandatory=true
     *      2. 设置确认回调ReturnCallback
     *
     * 消费端：消费端确认（保证每个消息被正确消费，此时broker才可以删除这个消息）
     *      spring.rabbitmq.listener.simple.acknowledge-mode=manual 手动ack
     *      1. 默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息
     *          问题：
     *              我们收到了很多消息，都自动回复给服务器ack，只有一个消息处理成功，宕机了，发生了消息丢失
     *          解决：
     *              开启消费者手动确认模式。只要我们没有明确告诉MQ，消息被正确消费，没有Ack，消息就一直是Unacked状态。
     *              即使Consumer宕机，消息不会丢失，会重新变为Ready，下一次有新的Consumer连接进来就发给他。
     *      2. 如何ack消息
     *          channel.basicAck(deliveryTag, false); 签收：业务成功完成就应该签收
     *          channel.basicNack(deliveryTag, false, false); 拒签：业务失败则拒签
     * @PostConstruct 在MyRabbitConfig对象创建完成之后，执行这个方法
     */
    @PostConstruct
    public void initRabbitTemplate() {
        // 设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 1. 只要消息抵达到Broker，ack就为true
             *
             * @param correlationData   当前消息的唯一关联数据（这个是消息的唯一id）
             * @param ack   消息是否成功收到
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {

                /**
                 * 1、做好消息确认机制（publisher、consumer【手动ack】）
                 * 2、每一个发送的消息都在数据库做好记录。定期将失败的消息再次发送一遍
                 */
                // 服务器收到了
                System.out.println("confirm...correlationData[" + correlationData + "]==>ack[" + ack + "]==>cause[" + cause + "]");
            }
        });

        // 设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递到指定的队列，就触发这个失败回调
             * @param message   投递失败的消息的详细信息
             * @param replyCode 回复的状态码
             * @param replyText 回复的文本内容
             * @param exchange  当时这个消息发送给哪个交换机
             * @param routingKey    当时这个消息使用哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                // 报错了，修改数据库当前消息的状态 -> 错误
                System.out.println("Fail Message[" + message + "]==>replyCode[" + replyCode + "]==>replyText[" + replyText + "]==>exchange[" + exchange + "]==>routingKey[" + routingKey + "]");
            }
        });
    }

}
