package com.zyj.gulimall.auth.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @program: gulimall
 * @ClassName ThirdPartyFeignService
 * @author: YaJun
 * @Date: 2021 - 12 - 21 - 20:12
 * @Package: com.zyj.gulimall.auth.feign
 * @Description:
 */
@FeignClient("gulimall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendCode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);

}
