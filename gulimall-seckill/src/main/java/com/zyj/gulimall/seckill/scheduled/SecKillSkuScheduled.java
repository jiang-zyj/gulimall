package com.zyj.gulimall.seckill.scheduled;

import com.zyj.gulimall.seckill.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @program: gulimall
 * @ClassName SecKillSkuScheduled
 * @author: YaJun
 * @Date: 2022 - 02 - 13 - 21:37
 * @Package: com.zyj.gulimall.seckill.scheduled
 * @Description: 秒杀商品的定时上架业务
 * 每天晚上3点：上架最近三天需要秒杀的商品
 * 当天00:00:00 - 23:59:59
 * 明天00:00:00 - 23:59:59
 * 后天00:00:00 - 23:59:59
 */
@Slf4j
@Service
public class SecKillSkuScheduled {

    @Autowired
    private SecKillService secKillService;

    @Autowired
    private RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";

    // TODO: 幂等性处理
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSecKillSkuLatestThreeDays() {
        // 1. 重复上架无需处理
        log.info("上架秒杀的商品信息...");
        // 分布式锁。锁的业务执行完成，状态已经更新完成。释放锁以后，其他人就会获取到最新的状态信息
        RLock lock = redissonClient.getLock(upload_lock);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            secKillService.uploadSecKillSkuLatestThreeDays();
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
