package com.zyj.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zyj.common.to.mq.SecKillOrderTo;
import com.zyj.common.utils.R;
import com.zyj.common.vo.MemberRespVo;
import com.zyj.gulimall.seckill.feign.CouponFeignService;
import com.zyj.gulimall.seckill.feign.ProductFeignService;
import com.zyj.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.zyj.gulimall.seckill.service.SecKillService;
import com.zyj.gulimall.seckill.to.SecKillSkuRedisTo;
import com.zyj.gulimall.seckill.vo.SecKillSessionWithSkus;
import com.zyj.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
@Slf4j
@Service
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

    /**
     * TODO: 上架秒杀商品的时候，每一个数据都有过期时间
     * TODO: 秒杀后续的流程，简化了收货地址等信息
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) {

        long start = System.currentTimeMillis();
        MemberRespVo respVo = LoginUserInterceptor.loginUser.get();

        // 1. 获取当前秒杀商品的详细信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        String json = hashOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        } else {
            SecKillSkuRedisTo redisTo = JSON.parseObject(json, SecKillSkuRedisTo.class);
            // 校验合法性
            Long startTime = redisTo.getStartTime();
            Long endTime = redisTo.getEndTime();
            long currentTime = new Date().getTime();
            long ttl = endTime - startTime;
            // 1. 校验时间的合法性
            if (currentTime >= startTime && currentTime <= endTime) {
                // 2. 校验随机码和商品id
                String randomCode = redisTo.getRandomCode();
                String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (randomCode.equals(key) && killId.equals(skuId)) {
                    // 3. 验证购物数量是否合理
                    if (num <= redisTo.getSeckillLimit()) {
                        // 4. 判断这个人是否已经买过了。幂等性；如果秒杀成功，就去redis占位：userId_SessionId_skuId
                        // SETNX
                        String redisKey = respVo.getId() + "_" + skuId;
                        // 自动过期
                        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
                        if (aBoolean) {
                            // 占位成功，说明从来没有买过
                            // 扣减信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                            // 20ms
                            boolean b = semaphore.tryAcquire(num);
                            if (b) {
                                // 秒杀成功
                                // 快速下单，发送MQ消息  10ms
                                String timeId = IdWorker.getTimeId();
                                SecKillOrderTo orderTo = new SecKillOrderTo();
                                orderTo.setOrderSn(timeId);
                                orderTo.setMemberId(respVo.getId());
                                orderTo.setNum(num);
                                orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
                                orderTo.setSkuId(redisTo.getSkuId());
                                orderTo.setSeckillPrice(redisTo.getSeckillPrice());

                                rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);

                                long end = System.currentTimeMillis();
                                log.info("总耗时时间..." + (end - start) + "毫秒");
                                return timeId;
                            }
                            return null;
                        } else {
                            // 说明已经买过了
                            return null;
                        }
                    }
                } else {
                    return null;
                }
            } else {

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
