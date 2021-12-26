package com.zyj.gulimall.auth.feign;

import com.zyj.common.utils.R;
import com.zyj.gulimall.auth.vo.SocialUser;
import com.zyj.gulimall.auth.vo.UserLoginVo;
import com.zyj.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program: gulimall
 * @ClassName MemberFeignService
 * @author: YaJun
 * @Date: 2021 - 12 - 22 - 20:51
 * @Package: com.zyj.gulimall.auth.feign
 * @Description:
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo registerVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R login(@RequestBody SocialUser socialUser) throws Exception;

}
