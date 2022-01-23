package com.zyj.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: gulimall
 * @ClassName GuliFeignConfig
 * @author: YaJun
 * @Date: 2022 - 01 - 11 - 20:48
 * @Package: com.zyj.gulimall.order.config
 * @Description: 增强Feign的请求，给请求中添加其他业务需要的数据
 */
@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 1. 使用RequestContextHolder获取到刚进来的请求
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    System.out.println("RequestInterceptor线程..." + Thread.currentThread().getId());
                    HttpServletRequest request = attributes.getRequest();   // 老请求
                    if (request != null) {
                        // 同步请求头数据: Cookie
                        String cookie = request.getHeader("Cookie");
                        template.header("Cookie", cookie);
                        //System.out.println("Feign在进行远程调用之前先进行requestInterceptor");
                    }
                }
            }
        };
    }

}
