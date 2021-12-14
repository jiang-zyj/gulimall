package com.zyj.gulimall.search.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName ProductFeignService
 * @author: YaJun
 * @Date: 2021 - 12 - 13 - 21:22
 * @Package: com.zyj.gulimall.search.feign
 * @Description:
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);


    @GetMapping("/product/brand/infos")
    R infos(@RequestParam("brandIds") List<Long> brandIds);
}
