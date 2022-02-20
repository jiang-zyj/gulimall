package com.zyj.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zyj.common.utils.R;
import com.zyj.gulimall.seckill.feign.CouponFeignService;
import com.zyj.gulimall.seckill.feign.ProductFeignService;
import com.zyj.gulimall.seckill.service.SecKillService;
import com.zyj.gulimall.seckill.to.SecKillSkuRedisTo;
import com.zyj.gulimall.seckill.vo.SecKillSessionWithSkus;
import com.zyj.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @program: gulimall
 * @ClassName SecKillServiceImpl
 * @author: YaJun
 * @Date: 2022 - 02 - 13 - 21:43
 * @Package: com.zyj.gulimall.seckill.service.impl
 * @Description:
 */
@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RedissonClient redissonClient;

    private final String SESSIONS_CACHE_PREFIX = "seckill:session:";

    private final String SKUKILL_CACHE_PREFIX = "seckill:skus:";

    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    // + 商品随机码

    @Override
    public void uploadSecKillSkuLatestThreeDays() {
        // 1. 扫描最近三天需要参与秒杀的活动
        R session = couponFeignService.getLatestThreeSession();
        if (session.getCode() == 0) {
            // 上架商品
            List<SecKillSessionWithSkus> sessionData = session.getData(new TypeReference<List<SecKillSessionWithSkus>>() {
            });
            // 缓存到redis
            // 1. 缓存活动信息
            saveSessionInfos(sessionData);
            // 2. 缓存活动的关联商品信息
            saveSessionSkuInfos(sessionData);
        }
    }

    /**
     * 返回当前时间可以参与的秒杀商品信息
     *
     * @return
     */
    @Override
    public List<SecKillSkuRedisTo> getCurrentSecKillSkus() {
        // 1. 确定当前时间属于哪个秒杀场次
        long time = new Date().getTime();
        // 获取redis中所有key
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");
        for (String key : keys) {
            // seckill:session:1645401600000_1645408800000
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            long start = Long.parseLong(split[0]);
            long end = Long.parseLong(split[1]);
            if (time >= start && time <= end) {
                // 2. 获取这个秒杀场次需要的所有商品信息
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> hashOps =
                        redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> list = hashOps.multiGet(range);
                if (list != null) {
                    List<SecKillSkuRedisTo> collect = list.stream().map(item -> {
                        SecKillSkuRedisTo redis = JSON.parseObject(item, SecKillSkuRedisTo.class);
                        //redis.setRandomCode();    // 当前秒杀已经开始了，随机码可以放出来
                        return redis;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }
        }
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSecKillInfo(Long skuId) {
        // 1. 找到所有需要参与秒杀活动的商品的key
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        Set<String> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                // 3_1  使用正则匹配商品的key
                String regx = "\\d_" + skuId;
                if (Pattern.matches(regx, key)) {
                    String json = hashOps.get(key);
                    SecKillSkuRedisTo skuRedisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
                    // 判断是否给商品随机码
                    long currentTime = new Date().getTime();
                    Long startTime = skuRedisTo.getStartTime();
                    Long endTime = skuRedisTo.getEndTime();
                    if (currentTime >= startTime && currentTime <= endTime) {
                    } else {
                        skuRedisTo.setRandomCode(null);
                    }
                    return skuRedisTo;
                }
            }
        }
        return null;
    }

    private void saveSessionInfos(List<SecKillSessionWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            if (!hasKey) {
                List<String> collect = session.getRelationSkus().stream().map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString()).collect(Collectors.toList());
                // 缓存活动信息
                redisTemplate.opsForList().leftPushAll(key, collect);
            }
        });
    }

    private void saveSessionSkuInfos(List<SecKillSessionWithSkus> sessionData) {
        sessionData.stream().forEach(session -> {
            // 准备Hash操作
            BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(secKillSkuVo -> {
                // 如果redis中没有该key，则进行添加，否则无需操作
                if (!hashOps.hasKey(secKillSkuVo.getPromotionSessionId().toString() + "_" + secKillSkuVo.getSkuId().toString())) {
                    // 缓存商品
                    SecKillSkuRedisTo redisTo = new SecKillSkuRedisTo();
                    // 1. sku的基本信息
                    R skuInfo = productFeignService.getSkuInfo(secKillSkuVo.getSkuId());
                    if (skuInfo.getCode() == 0) {
                        SkuInfoVo info = skuInfo.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfo(info);
                    }
                    // 2. sku的秒杀信息
                    BeanUtils.copyProperties(secKillSkuVo, redisTo);

                    // 3. 设置当前商品的秒杀时间信息
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());

                    // 4. 商品的随机码？ seckill?skuId=1&key=saefsad
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisTo.setRandomCode(token);

                    // 如果当前这个场次的商品的库存信息已经上架就不需要上架了
                    // 5. 使用库存作为分布式的信号量     限流；
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    // 商品可以秒杀的数量作为信号量
                    semaphore.trySetPermits(secKillSkuVo.getSeckillCount());

                    String jsonString = JSON.toJSONString(redisTo);
                    hashOps.put(secKillSkuVo.getPromotionSessionId().toString() + "_" + secKillSkuVo.getSkuId().toString(), jsonString);
                }
            });
        });
    }

}
