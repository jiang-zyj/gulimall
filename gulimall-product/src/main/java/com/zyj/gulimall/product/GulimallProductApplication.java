package com.zyj.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
 */
@EnableFeignClients(basePackages = "com.zyj.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.zyj.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
