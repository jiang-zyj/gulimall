package com.zyj.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * 给容器中放一个JSON的消息转换器，让SpringBoot使用我们自定义的MessageConverter
     *
     * @return 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
