package com.zyj.gulimall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用RabbitMQ
 * 1. 引入amqp场景：RabbitAutoConfiguration就会自动生效
 * 2. 给容器中自动配置了：
 *      RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 *      所有的属性都是在这里进行绑定 spring.rabbitmq
 *      @ConfigurationProperties(prefix = "spring.rabbitmq")
 *      public class RabbitProperties
 * 3. 给配置文件中配置 spring.rabbitmq.xxx
 * 4. 开启注解：@EnableRabbit
 * 5. 监听消息：使用@RabbitListener，前提是开启注解@EnableRabbit
 * @RabbitListener: 标注在类 + 方法上（监听哪些队列）
 * @RabbitHandler: 标注在方法上（重载区分不同的消息）
 */
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
