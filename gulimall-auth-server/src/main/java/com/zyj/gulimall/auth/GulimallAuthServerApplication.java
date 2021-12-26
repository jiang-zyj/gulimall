package com.zyj.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * SpringSession核心原理
 * 1、@EnableRedisHttpSession导入RedisHttpSessionConfiguration配置
 *      1、给容器中添加了一个组件
 *          SessionRepository ==》RedisSessionRepository ==》 RedisIndexedSessionRepository：Redis操作session。session的增删改查封装类
 *      2、SessionRepositoryFilter ==》Filter：session存储过滤器；每个请求过来都必须经过这个filter
 *          1、创建的时候，就自动从容器中获取到了SessionRepository
 *      SessionRepositoryFilter中的方法
 *      protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
 *         request.setAttribute(SESSION_REPOSITORY_ATTR, this.sessionRepository);
 *         SessionRepositoryFilter<S>.SessionRepositoryRequestWrapper wrappedRequest = new SessionRepositoryFilter.SessionRepositoryRequestWrapper(request, response);
 *         SessionRepositoryFilter.SessionRepositoryResponseWrapper wrappedResponse = new SessionRepositoryFilter.SessionRepositoryResponseWrapper(wrappedRequest, response);
 *
 *         try {
 *             filterChain.doFilter(wrappedRequest, wrappedResponse);
 *         } finally {
 *             wrappedRequest.commitSession();
 *         }
 *      }
 *          2、从代码中可以看到，原始的request、response都被包装成了 SessionRepositoryFilter<S>.SessionRepositoryRequestWrapper，SessionRepositoryFilter.SessionRepositoryResponseWrapper
 *          3、以后获取session，都需要调用request.getSession()方法
 *          4、然后被封装后就变成了 wrappedRequest.getSession();
 *          5、上一步的getSession又是从SessionRepository中获取到的，其实就是从RedisIndexedSessionRepository中获取到的。
 *
 *
 * 原理：装饰者模式
 *      自动延期；redis中的数据也是有过期时间的。
 *
 *
 */
@EnableRedisHttpSession // 整合redis作为session存储
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
