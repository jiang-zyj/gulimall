package com.zyj.gulimall.order.config;

import com.zyj.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: gulimall
 * @ClassName OrderWebConfigure
 * @author: YaJun
 * @Date: 2022 - 01 - 06 - 20:48
 * @Package: com.zyj.gulimall.order.config
 * @Description: Web配置
 */
@Configuration
public class OrderWebConfigure implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
    }
}
