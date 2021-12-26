package com.zyj.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 1. 整合 Mybatis-Plus
 *      1) 导入依赖
 *      2) 配置
 *          1. 配置数据源
 *              1. 数据库驱动
 *              2. 在 application.yml 中配置数据源等相关信息
 *          2. 配置Mybatis-Plus
 *              1. 使用 MapperScan
 *              2. 告诉Mybatis-Plus，SQL映射文件位置
 *
 *  2. 逻辑删除：
 *      1. 配置全局的逻辑删除规则(省略)
 *      2. 配置逻辑删除的组件Bean(省略)
 *      3. 给Bean加上逻辑删除注解@TableLogic
 *
 *  3. JSR303: Java 规范提案，规定了数据校验的标准
 *      1. 给Bean添加校验注解: javax.validation.constraints，并定义自己的message提示
 *      2. 在给需要开启校验功能的Controller上标注 @Valid，以此来开启校验功能
 *          效果：校验错误以后会有默认的响应
 *      3. 给校验的bean后紧跟一个BindingResult，就可以获取到校验的结果
 *      4. 分组校验（多场景的复杂校验）
 *          1. 给校验注解标注什么时候需要进行校验
 *          @NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
 *          2. 给Controller中需要对对象校验的方法加上 @Validated(value = {XXXGroup.class})
 *          3. 默认没有指定分组的校验注解(比如@NotBlank)，在分组校验情况下 (@Validated(value = {XXXGroup.class})) 不生效，只能在 @Valid 注解的情况下生效
 *      5. 自定义校验（示例：BrandEntity中的showStatus属性）
 *          1. 编写一个自定义的校验注解
 *          2. 编写一个自定义的校验器 ConstraintValidator
 *          3. 关联自定义的校验器和自定义的校验注解
 *             @Documented
             * @Constraint(validatedBy = {ListValueConstraintValidator.class 【可以指定多个不同的校验器，适配不同类型的校验】})
             * @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
             * @Retention(RUNTIME)
 *  4. 统一的异常处理
 *  @ControllerAdvice
 *      1. 编写异常处理类，使用@ControllerAdvice。
 *      2. 使用@ExceptionHandler标注方法可以处理的异常。
 *
 *  5. 模板引擎
 *      1. thymeleaf-starter：关闭缓存
 *      2. 静态资源都放在static文件夹下就可以按照路径直接访问
 *      3. 页面放在templates下，直接访问
 *          SpringBoot，访问项目的时候，默认会找index.html
 *      4. 不重启服务器更新数据 dev-tools; 利用 control + shift + f9 刷新页面
 *
 *  6. 整合redis
 *      1. 引入data-redis-starter
 *      2. 简单配置redis的host等信息
 *      3. 使用SpringBoot自动配置的StringRedisTemplate来操作redis
 *      redis -> map<String, String>
 *  7. 整合redission作为分布式锁等功能框架
 *      1. 引入依赖：redisson
 *      2. 配置redisson
 *
 *  8. 整合SpringCache简化缓存开发
 *      1. 引入依赖
 *          spring-boot-starter-cache、spring-boot-starter-data-redis
 *      2. 写配置
 *          1). 自动配置了哪些？
 *              CacheAutoConfiguration 会导入 RedisCacheConfiguration
 *              自动配好了缓存管理器RedisCacheManager
 *          2). 配置使用redis作为缓存
 *              spring.cache.type=redis
 *      3. 测试使用缓存
 *          @Cacheable: Triggers cache population. 触发将数据保存到缓存的操作
 *          @CacheEvict: Triggers cache eviction.  触发将数据从缓存中删除的操作
 *          @CachePut: Updates the cache without interfering with the method execution. 不影响方法执行更新缓存
 *          @Caching: Regroups multiple cache operations to be applied on a method. 组合以上多个操作
 *          @CacheConfig: Shares some common cache-related settings at class-level. 在类级别共享缓存的相同配置
 *          1. 开启缓存功能 @EnableCaching
 *          2. 只需要使用注解就能完成缓存操作
 *      4. 原理：
 *          CacheAutoConfiguration -> RedisCacheConfiguration ->
 *          自动配置了RedisCacheManager -> 初始化所有的缓存 -> 每个缓存决定使用什么配置
 *          -> 如果redisCacheConfiguration有，就用已有的，如果没有，就用默认配置
 *          -> 想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration即可
 *          -> 就会应用到当前的RedisCacheManager管理的所有缓存分区中
 */

@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.zyj.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.zyj.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
