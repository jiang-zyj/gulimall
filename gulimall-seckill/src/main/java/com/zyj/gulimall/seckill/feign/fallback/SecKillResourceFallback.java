package com.zyj.gulimall.seckill.feign.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.zyj.common.utils.R;
import lombok.extern.slf4j.Slf4j;

/**
 * @program: gulimall
 * @ClassName SecKillResourceFallback
 * @author: YaJun
 * @Date: 2022 - 02 - 28 - 21:18
 * @Package: com.zyj.gulimall.product.feign.fallback
 * @Description: 自定义限流类
 */
@Slf4j
public class SecKillResourceFallback {

    public static R resourceFallbackMethod(BlockException e) {
        log.error("getCurrentSecKillSkusResource资源被限流了...通过fallbackClass指定方法...");
        return null;
    }

}
