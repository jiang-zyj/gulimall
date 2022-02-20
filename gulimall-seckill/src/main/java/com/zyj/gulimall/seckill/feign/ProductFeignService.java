package com.zyj.gulimall.seckill.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @program: gulimall
 * @ClassName ProductFeignService
 * @author: YaJun
 * @Date: 2022 - 02 - 14 - 20:05
 * @Package: com.zyj.gulimall.seckill.feign
 * @Description:
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfo(@PathVariable("skuId") Long skuId);

}
