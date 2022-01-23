package com.zyj.gulimall.ware.feign;

import com.zyj.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @program: gulimall
 * @ClassName MemberFeignService
 * @author: YaJun
 * @Date: 2022 - 01 - 12 - 21:13
 * @Package: com.zyj.gulimall.ware.feign
 * @Description:
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R addrInfo(@PathVariable("id") Long id);

}
