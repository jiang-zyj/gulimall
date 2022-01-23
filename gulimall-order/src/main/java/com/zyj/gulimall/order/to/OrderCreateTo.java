package com.zyj.gulimall.order.to;

import com.zyj.gulimall.order.entity.OrderEntity;
import com.zyj.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName OrderCreateTo
 * @author: YaJun
 * @Date: 2022 - 01 - 13 - 21:58
 * @Package: com.zyj.gulimall.order.to
 * @Description: 封装订单创建成功的数据
 */
@Data
public class OrderCreateTo {

    /**
     * 订单实体数据
     */
    private OrderEntity order;

    /**
     * 购物项
     */
    private List<OrderItemEntity> orderItems;

    /**
     * 订单计算的应付价格
     */
    private BigDecimal payPrice;

    /**
     * 运费
     */
    private BigDecimal fare;

}
