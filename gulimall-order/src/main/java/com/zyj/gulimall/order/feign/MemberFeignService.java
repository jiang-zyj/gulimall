package com.zyj.gulimall.order.feign;

import com.zyj.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName MemberFeignService
 * @author: YaJun
 * @Date: 2022 - 01 - 06 - 21:44
 * @Package: com.zyj.gulimall.order.feign
 * @Description: 远程连接Member服务
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/getAddress")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);

}
