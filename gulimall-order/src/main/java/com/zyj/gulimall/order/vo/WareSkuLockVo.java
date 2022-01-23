package com.zyj.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName WareSkuLockVo
 * @author: YaJun
 * @Date: 2022 - 01 - 15 - 23:29
 * @Package: com.zyj.gulimall.order.vo
 * @Description: 库存锁定vo
 */
@Data
public class WareSkuLockVo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 需要锁定的所有库存信息
     */
    private List<OrderItemVo> locks;

}
