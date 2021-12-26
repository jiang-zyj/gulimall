package com.zyj.gulimall.auth.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: gulimall
 * @ClassName WebConfig
 * @author: YaJun
 * @Date: 2021 - 12 - 20 - 21:47
 * @Package: com.zyj.gulimall.auth.config
 * @Description:
 */
@Component
public class GulimallWebConfig implements WebMvcConfigurer {

    /**
     * 视图映射
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 需要自定义处理
        //registry.addViewController("/login.html").setViewName("login");
        // 只是get请求能映射
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
