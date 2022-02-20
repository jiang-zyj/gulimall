package com.zyj.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: gulimall
 * @ClassName SecKillSkuVo
 * @author: YaJun
 * @Date: 2022 - 02 - 13 - 22:26
 * @Package: com.zyj.gulimall.seckill.vo
 * @Description:
 */
@Data
public class SecKillSkuVo {

    private Long id;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

}
