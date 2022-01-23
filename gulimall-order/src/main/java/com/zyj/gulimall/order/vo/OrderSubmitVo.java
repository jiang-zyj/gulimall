package com.zyj.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @program: gulimall
 * @ClassName OrderSubmitVo
 * @author: YaJun
 * @Date: 2022 - 01 - 13 - 20:32
 * @Package: com.zyj.gulimall.order.vo
 * @Description: 封装订单提交的数据
 */
@Data
public class OrderSubmitVo {

    // 无需提交需要购买的商品，直接在从购物车中获取一遍
    // 优惠、发票信息
    // 用户相关信息、直接去session中获取用户信息

    /**
     * 收货地址的id
     */
    private Long addrId;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * 防重令牌
     */
    private String orderToken;

    /**
     * 应付价格
     * 可以做个功能：验价。即验证订单中的总价格与购物车选中的商品的总价格是否相等
     * 这样就可以在两个价格不同时，提示用户有一些商品的价格已经发生改变，请酌情购买
     */
    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;


}
