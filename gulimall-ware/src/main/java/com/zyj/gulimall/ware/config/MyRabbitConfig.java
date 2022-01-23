package com.zyj.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName MyRabbitConfig
 * @author: YaJun
 * @Date: 2022 - 01 - 18 - 22:06
 * @Package: com.zyj.gulimall.ware.config
 * @Description: RabbitMQ的配置信息
 */
@Configuration
public class MyRabbitConfig {

    /**
     * 给容器中放一个JSON的消息转换器，让SpringBoot使用我们自定义的MessageConverter
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    //@RabbitListener(queues = "stock.release.stock.queue")
    //public void listener(Message message) {
    //
    //}

    /**
     * 库存服务默认交换机
     * @return
     */
    @Bean
    public Exchange stockEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete,
        //			@Nullable Map<String, Object> arguments
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue stockDelayQueue() {
        /**
         * x-dead-letter-exchange:stock-event-exchange
         * x-dead-letter-routing-key:stock.release
         * x-message-ttl:120000
         */
        Map<String, Object> args = new HashMap<>(3);
        args.put("x-dead-letter-exchange", "stock-event-exchange");
        args.put("x-dead-letter-routing-key", "stock.release");
        args.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, args);
    }

    /**
     * 交换机与普通队列进行绑定
     * @return
     */
    @Bean
    public Binding stockReleaseBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    /**
     * 交换机与延迟队列进行绑定
     * @return
     */
    @Bean
    public Binding stockLockedBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey,
        //			@Nullable Map<String, Object> arguments
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }

}
