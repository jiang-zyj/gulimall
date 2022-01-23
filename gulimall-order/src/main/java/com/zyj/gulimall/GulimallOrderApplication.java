package com.zyj.gulimall;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

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
 *
 *
 * 本地事务失效问题：
 *      同一个对象内事务方法互调默认失效，原因：绕过来代理对象
 *      事务是使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 *      1. 引入spring-boot-starter-aop，这个引入了aspectj
 *      2. @EnableAspectJAutoProxy；开启 aspectj 动态代理功能。以后的所有的动态代理都是aspectj创建的（即使没有接口也可以创建动态代理）。
 *          exposeProxy = true 对外暴露代理对象
 *      3. 本类互调使用代理对象
 *         OrderServiceImpl orderService = (OrderServiceImpl) AopContext.currentProxy();
 *         orderService.b();
 *         orderService.c();
 *
 * Seata控制分布式事务
 *  1. 每一个微服务的数据库先必须创建undo_log表
 *  2. 安装事务协调器：seata-server：https://github.com/seata/seata/releases
 *  3. 整合
 *      1. 导入依赖 spring-cloud-starter-alibaba-seata  seata-all-0.7.1
 *      2. 解压并启动seata-server
 *          registry.conf：注册中心配置；修改 registry type=nacos
 *          file.conf：
 *      3. 所有想要用到分布式事务的微服务必须使用seata DataSourceProxy代理自己的数据源
 *      4. 每个微服务，都必须导入
 *          file.config
 *          registry.config
 *          file.config 还需要配置 service.vgroup_mapping：vgroup_mapping.{application.name}-fescar-service-group
 *      5. 启动测试分布式事务
 *      6. 给分布式大事务的入口标注：@GlobalTransactional
 *      7. 每一个分支事务标注：@Transactional
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@EnableRabbit
@SpringBootApplication
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
