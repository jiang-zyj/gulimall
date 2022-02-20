package com.zyj.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @program: gulimall
 * @ClassName MyRedissonConfig
 * @author: YaJun
 * @Date: 2021 - 12 - 05 - 21:51
 * @Package: com.zyj.gulimall.product.config
 * @Description:
 */
@Configuration
public class MyRedissonConfig {

    /**
     * 所有对redis的使用都是通过Redisson对象来进行操作
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        // 1. 创建配置
        Config config = new Config();

        // Redis url should start with redis:// or rediss://
        config.useSingleServer().setAddress("redis://192.168.241.135:6379");


        // 2. 根据Config创建出RedissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
