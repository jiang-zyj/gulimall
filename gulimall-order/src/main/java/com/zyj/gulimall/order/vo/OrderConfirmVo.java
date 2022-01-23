package com.zyj.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @program: gulimall
 * @ClassName OrderConfirmVo
 * @author: YaJun
 * @Date: 2022 - 01 - 06 - 21:16
 * @Package: com.zyj.gulimall.order.vo
 * @Description: 订单确认页需要的数据
 */
//@Data
public class OrderConfirmVo {

    /**
     * 收货地址，ums_member_receive_address 表
     */
    @Getter
    @Setter
    private List<MemberAddressVo> address;

    /**
     * 所有选中的购物项
     */
    @Getter
    @Setter
    private List<OrderItemVo> items;

    // 发票信息...发票记录

    /**
     * 优惠券信息...
     * 积分
     */
    @Getter
    @Setter
    private Integer integration;

    /**
     * sku是否有货
     */
    @Getter
    @Setter
    private Map<Long, Boolean> stocks;

    private Integer count;

    /**
     * 防重令牌
     */
    @Getter
    @Setter
    private String orderToken;

    /**
     * 订单总额
     */
    private BigDecimal total;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        return getTotal();
    }

    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }
}
