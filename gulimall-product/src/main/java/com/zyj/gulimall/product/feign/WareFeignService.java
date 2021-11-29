package com.zyj.gulimall.product.feign;

import com.zyj.common.to.SkuHasStockVo;
import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName WareFeignService
 * @author: YaJun
 * @Date: 2021 - 11 - 29 - 22:45
 * @Package: com.zyj.gulimall.product.feign
 * @Description:
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 解决返回数据又要转换的问题
     * 1. R 设计的时候可以加上泛型
     * 2. 直接返回自己需要的数据类型
     * 3. 自己封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R<List<SkuHasStockVo>> getSkusHasStock(@RequestBody List<Long> skuIds);

}
