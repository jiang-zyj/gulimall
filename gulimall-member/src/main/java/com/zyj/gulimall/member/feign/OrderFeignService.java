package com.zyj.gulimall.member.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @program: gulimall
 * @ClassName OrderFeignService
 * @author: YaJun
 * @Date: 2022 - 02 - 10 - 20:29
 * @Package: com.zyj.gulimall.member.feign
 * @Description:
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);

}
