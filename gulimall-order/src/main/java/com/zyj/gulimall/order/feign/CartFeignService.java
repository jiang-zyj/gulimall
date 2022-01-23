package com.zyj.gulimall.order.feign;

import com.zyj.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName CartFeignService
 * @author: YaJun
 * @Date: 2022 - 01 - 06 - 22:02
 * @Package: com.zyj.gulimall.order.feign
 * @Description:
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();

}
