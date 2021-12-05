package com.zyj.gulimall.product.web;

import com.zyj.gulimall.product.entity.CategoryEntity;
import com.zyj.gulimall.product.service.CategoryService;
import com.zyj.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName IndexController
 * @author: YaJun
 * @Date: 2021 - 11 - 30 - 21:31
 * @Package: com.zyj.gulimall.product.web
 * @Description:
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        // 1. 查出所有的一级分类的菜单
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();

        model.addAttribute("categories", categoryEntities);
        return "index";
    }


    // index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {

        Map<String, List<Catelog2Vo>> catelogJson = categoryService.getCatelogJson();
        return catelogJson;
    }


    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 1. 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        // 2. 加锁
        lock.lock();    // 阻塞式等待。默认假的锁都是30s时间
        // 1). 锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动过期被删掉
        // 2). 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除。
        try {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            // 3. 解锁    假设解锁代码没有运行，redisson会不会出现死锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }


        return "hello";
    }
}
