package com.zyj.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

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
 *
 *  4. 统一的异常处理
 *  @ControllerAdvice
 *      1.
 */
@EnableDiscoveryClient
@MapperScan("com.zyj.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
