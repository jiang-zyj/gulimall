package com.zyj.gulimall.cart.config;

import com.zyj.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: gulimall
 * @ClassName GulimallWebConfig
 * @author: YaJun
 * @Date: 2021 - 12 - 27 - 22:13
 * @Package: com.zyj.gulimall.cart.config
 * @Description:
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
