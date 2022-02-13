package com.zyj.gulimall.member.config;

import com.zyj.gulimall.member.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: gulimall
 * @ClassName MemberWebConfig
 * @author: YaJun
 * @Date: 2022 - 02 - 09 - 21:41
 * @Package: com.zyj.gulimall.member.config
 * @Description:
 */
@Configuration
public class MemberWebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
