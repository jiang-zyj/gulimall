package com.zyj.gulimall.member.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @program: gulimall
 * @ClassName CouponFeignService
 * @author: YaJun
 * @Date: 2021 - 11 - 02 - 20:18
 * @Package: com.zyj.gulimall.member.feign
 * @Description: 这是一个声明式的远程调用
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    R memberCoupons();

}
