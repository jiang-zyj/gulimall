package com.zyj.gulimall.product.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @program: gulimall
 * @ClassName SecKillFeignService
 * @author: YaJun
 * @Date: 2022 - 02 - 20 - 19:19
 * @Package: com.zyj.gulimall.product.feign
 * @Description:
 */
@FeignClient("gulimall-seckill")
public interface SecKillFeignService {

    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSecKillInfo(@PathVariable("skuId") Long skuId);

}
