package com.zyj.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1. 整合Sentinel
 *      1. 导入依赖 spring-cloud-starter-alibaba-sentinel
 *      2. 下载sentinel的控制台
 *      3. 配置sentinel控制台地址信息
 *      4. 在控制台调整参数【默认所有的流控设置保存在内存中，重启后失效】
 *
 * 2. 每一个服务都导入 spring-boot-starter-actuator，并配置 management.endpoints.web.exposure.include=*
 * 3. 自定义sentinel流控返回
 */
//@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
