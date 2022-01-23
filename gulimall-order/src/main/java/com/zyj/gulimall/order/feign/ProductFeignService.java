package com.zyj.gulimall.order.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @program: gulimall
 * @ClassName ProductFeignService
 * @author: YaJun
 * @Date: 2022 - 01 - 13 - 22:34
 * @Package: com.zyj.gulimall.order.feign
 * @Description: 商品服务
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuInfoBySkuId(@PathVariable("id") Long skuId);

}
