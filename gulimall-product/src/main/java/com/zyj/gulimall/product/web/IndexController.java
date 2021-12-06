package com.zyj.gulimall.product.web;

import com.zyj.gulimall.product.entity.CategoryEntity;
import com.zyj.gulimall.product.service.CategoryService;
import com.zyj.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    private StringRedisTemplate redisTemplate;

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
        //lock.lock(10, TimeUnit.SECONDS); // 10s后自动解锁，自动解锁时间一定要大于业务的执行时间。
        // 问题：lock.lock(10, TimeUnit.SECONDS); 在锁到期之后，不会自动续期
        // 1. 如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时时间就是我们指定的时间
        // 2. 如果我们未指定锁的超时时间，就使用30 * 1000【lockWatchdogTimeout看门狗的默认时间】
        // 只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，每隔10s都会自动续期，续成30s
        // internalLockLeaseTime / 3    【看门狗时间】 / 3     即这里的10s，在10s后会请求自动续期


        // 最佳实战
        // 1. lock.lock(30, TimeUnit.SECONDS); 省掉了整个续期操作，但是可能会出现业务时间过长导致锁被释放，但是我们可以在预期就估算好值
        // 再说了，一个业务30s，差不多也是系统崩了。最后手动解锁
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

    /**
     * 读写锁：
     * 保证一定能读到最新数据，修改期间，写锁是一个排它锁（互斥锁、独享锁）。读锁是一个共享锁
     * 写锁没释放，读就必须等待
     * 读 + 读：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
     * 写 + 读：等待写锁释放
     * 写 + 写：阻塞方式
     * 读 + 写：有读锁。写也需要等待。
     * 只要有写的存在，都必须等待
     * @return
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            // 1. 改数据加写锁，读数据加写锁
            rLock.lock();
            System.out.println("写锁加锁成功..." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放成功..." + Thread.currentThread().getId());
        }
        return s;
    }


    @ResponseBody
    @GetMapping("/read")
    public String readValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        // 加读锁
        RLock rLock = lock.readLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("读锁加锁成功..." + Thread.currentThread().getId());
            s = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放成功..." + Thread.currentThread().getId());
        }
        return s;
    }


    /**
     * 车库停车
     * 现有3个车位
     * 信号量也可以用作分布式限流
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        //park.acquire(); // 阻塞方法；获取一个信号；相当于获取一个值，占一个车位
        boolean b = park.tryAcquire();  // 非阻塞，尝试一下，不行就算了
        if (b) {
            // 执行业务
        } else {
            return "error";
        }
        return "ok=>" + b;
    }

    @ResponseBody
    @GetMapping("/go")
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release(); // 释放一个车位

        return "ok";
    }

    /**
     * 放假，锁门，等待所有人走完才能锁门
     * 5个班全部走完，我们可以锁大门
     */
    @ResponseBody
    @GetMapping("/lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();   // 等待闭锁都完成

        return "放假了...";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();   // 计数减一

        return id + "班的人都走了...";
    }

}
