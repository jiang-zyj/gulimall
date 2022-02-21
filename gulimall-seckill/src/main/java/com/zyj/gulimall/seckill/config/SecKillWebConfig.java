package com.zyj.gulimall.seckill.config;

import com.zyj.gulimall.seckill.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: gulimall
 * @ClassName SecKillWebConfig
 * @author: YaJun
 * @Date: 2022 - 02 - 20 - 21:48
 * @Package: com.zyj.gulimall.seckill.config
 * @Description:
 */
@Configuration
public class SecKillWebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
