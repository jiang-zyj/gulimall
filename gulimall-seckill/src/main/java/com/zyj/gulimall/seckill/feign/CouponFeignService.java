package com.zyj.gulimall.seckill.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @program: gulimall
 * @ClassName CouponFeignService
 * @author: YaJun
 * @Date: 2022 - 02 - 13 - 21:45
 * @Package: com.zyj.gulimall.seckill.feign
 * @Description:
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/latestThreeSession")
    R getLatestThreeSession();

}
