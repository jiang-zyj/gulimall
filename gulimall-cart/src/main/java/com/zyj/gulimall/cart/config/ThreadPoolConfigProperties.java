package com.zyj.gulimall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @program: gulimall
 * @ClassName ThreadPoolConfigProperties
 * @author: YaJun
 * @Date: 2021 - 12 - 20 - 20:37
 * @Package: com.zyj.gulimall.product.config
 * @Description:
 */
@ConfigurationProperties(prefix = "gulimall.thread")
@Component
@Data
public class ThreadPoolConfigProperties {

    private Integer coreSize;

    private Integer maxSize;

    private Integer keepAliveTime;

}
