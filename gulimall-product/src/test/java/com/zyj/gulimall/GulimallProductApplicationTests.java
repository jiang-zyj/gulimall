package com.zyj.gulimall;

import com.zyj.gulimall.product.GulimallProductApplication;
import com.zyj.gulimall.product.entity.BrandEntity;
import com.zyj.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 * 1. 引入 oss-starter
 * 2. 配置key、endpoint等信息
 * 3. 使用OSSClient进行上传
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GulimallProductApplication.class)
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testStringRedisTemplate() {
        // hello    world
        ValueOperations<String, String> ops =
                stringRedisTemplate.opsForValue();
        // 保存
        ops.set("hello", "world_" + UUID.randomUUID().toString());

        // 查询
        String hello = ops.get("hello");
        System.out.println("之前保存的数据：" + hello);
    }

    //@Autowired
    //OSSClient ossClient;
    //
    //@Test
    //public void upload() throws FileNotFoundException {
    //    // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
    //    //String endpoint = "oss-cn-shenzhen.aliyuncs.com";
    //    //// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
    //    //String accessKeyId = "LTAI5tG98eYi47ft4Ao8hqZp";
    //    //String accessKeySecret = "nbtDhE2fRiORNvzz8QdRhmMbl699Nf";
    //
    //    // 创建OSSClient实例。
    //    //ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    //
    //    // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
    //    InputStream inputStream = new FileInputStream("Z:\\Java视频\\5.尚硅谷全套JAVA教程—项目实战（20.64GB）\\大型电商--谷粒商城\\基础篇\\资料\\pics\\5b5e74d0978360a1.jpg");
    //    // 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
    //    ossClient.putObject("gulimall-jiang-dr", "phone.jpg", inputStream);
    //
    //    // 关闭OSSClient。
    //    ossClient.shutdown();
    //    System.out.println("上传成功");
    //}

    @Test
    public void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");

        brandService.save(brandEntity);
        System.out.println("保存成功");

    }



}
