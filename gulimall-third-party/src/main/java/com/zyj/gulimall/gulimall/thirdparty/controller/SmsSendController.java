package com.zyj.gulimall.gulimall.thirdparty.controller;

import com.zyj.common.utils.R;
import com.zyj.gulimall.gulimall.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: gulimall
 * @ClassName SmsSendController
 * @author: YaJun
 * @Date: 2021 - 12 - 21 - 20:07
 * @Package: com.zyj.gulimall.gulimall.thirdparty.controller
 * @Description:
 */
@RestController
@RequestMapping
public class SmsSendController {

    @Autowired
    private SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }

}
