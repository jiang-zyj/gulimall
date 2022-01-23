package com.zyj.gulimall.ware.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @program: gulimall
 * @ClassName ProductFeignService
 * @author: YaJun
 * @Date: 2021 - 11 - 21 - 19:02
 * @Package: com.zyj.gulimall.feign
 * @Description:
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     *
     *
     *
     *  这里可以写两种请求方式：
     *      1. 让所有请求过网关
     *          1.1 @FeignClient("gulimall-gateway")，给网关的机器发请求
     *          1.2 /api//product/skuinfo/info/{skuId}
     *      2. 直接让后台指定服务处理
     *          2.1 @FeignClient("gulimall-product")
     *          2.2 /product/skuinfo/info/{skuId}
     * @param skuId
     * @return
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

}
