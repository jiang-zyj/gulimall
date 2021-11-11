package com.zyj.gulimall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1. 如何使用Nacos作为配置中心统一管理配置
 *      1. 引入依赖
 *          <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-nacos-com.zyj.gulimall.geteway.config</artifactId>
 *         </dependency>
 *      2. 创建一个 bootstrap.properties
 *          spring.application.name=gulimall-coupon
 *          spring.cloud.nacos.com.zyj.gulimall.geteway.config.server-addr=127.0.0.1:8848
 *      3. 需要给配置中心默认添加一个叫 数据集（Data Id）gulimall-coupon.properties。默认规则就是：应用名.properties
 *      4. 给 应用名.properties 中添加任何配置
 *      5. 动态获取配置
 *          1. @RefreshScope：动态获取并刷新配置
 *          2. @Value("${配置项的名}")：获取到配置
 *          如果配置中心和当前应用的配置文件中，都配置了相同的项，优先使用配置中心的配置
 *
 * 2. 细节
 *      1. 命名空间：配置隔离
 *          默认：public（保留空间）；默认新增的所有配置都在public空间
 *          1. 开发、测试、生产：利用命名空间来做环境隔离
 *              注意：在 bootstrap.properties 配置上，需要指定使用哪个命名空间的配置文件（默认是public命名空间）
 *              spring.cloud.nacos.com.zyj.gulimall.geteway.config.namespace=2c9de239-f731-4032-b667-0abb49534845
 *          2. 基于每一个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 *
 *      2. 配置集：所有的配置的集合
 *
 *      3. 配置集ID：类似于配置文件名
 *          Data ID：类似于配置文件名
 *
 *      4. 配置分组：
 *          默认所有的配置集都属于：DEFAULT_GROUP
 *          1111,618,1212
 *
 * 每个微服务创建自己的命名空间，使用配置分组来区分dev、test、prop环境
 *
 * 3. 同时加载多个配置集
 *  1. 微服务任何配置信息，任何配置文件都可以放在配置中心中
 *  2. 只需要在 bootstrap.properties 说明加载配置中心中哪些配置文件即可
 *  3. @Value，@ConfigurationProperties...
 *      以前SpringBoot任何方法从配置文件中获取值的方法，都能使用
 *      配置中心有的配置优先使用配置中心的
 */
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
