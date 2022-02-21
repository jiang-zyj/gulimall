package com.zyj.gulimall.seckill.service;

import com.zyj.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SecKillService
 * @author: YaJun
 * @Date: 2022 - 02 - 13 - 21:43
 * @Package: com.zyj.gulimall.seckill.service
 * @Description:
 */
public interface SecKillService {

    void uploadSecKillSkuLatestThreeDays();

    List<SecKillSkuRedisTo> getCurrentSecKillSkus();

    SecKillSkuRedisTo getSkuSecKillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
