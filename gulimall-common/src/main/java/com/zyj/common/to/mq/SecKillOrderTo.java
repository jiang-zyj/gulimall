package com.zyj.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: gulimall
 * @ClassName SecKillOrderTo
 * @author: YaJun
 * @Date: 2022 - 02 - 20 - 22:51
 * @Package: com.zyj.common.to.mq
 * @Description:
 */
@Data
public class SecKillOrderTo {

    /**
     * 订单号
     */
    private String orderSn;

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
    private Integer num;

    /**
     * 会员id
     */
    private Long memberId;



}
