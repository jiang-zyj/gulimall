package com.zyj.gulimall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.common.utils.PageUtils;
import com.zyj.common.utils.Query;
import com.zyj.gulimall.coupon.dao.SeckillSessionDao;
import com.zyj.gulimall.coupon.entity.SeckillSessionEntity;
import com.zyj.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zyj.gulimall.coupon.service.SeckillSessionService;
import com.zyj.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key);
        }

        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLatestThreeSession() {
        // 计算最近三天的时间
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", getStartTime(), getEndTime()));

        if (list != null && list.size() != 0) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationSkus(relationEntities);
                return session;
            }).collect(Collectors.toList());
            return collect;
        }

        return null;
    }

    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(now, LocalTime.MIN);

        String format = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDate plusDays = now.plusDays(2);
        LocalDateTime end = LocalDateTime.of(plusDays, LocalTime.MAX);
        String format = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}