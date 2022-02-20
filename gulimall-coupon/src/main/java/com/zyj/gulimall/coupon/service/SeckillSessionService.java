package com.zyj.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.common.utils.PageUtils;
import com.zyj.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author Jiang
 * @email 2412305937@qq.com
 * @date 2021-11-01 22:16:13
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionEntity> getLatestThreeSession();
}

