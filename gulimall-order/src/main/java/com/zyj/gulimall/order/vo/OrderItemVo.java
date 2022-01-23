package com.zyj.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName OrderItemVo
 * @author: YaJun
 * @Date: 2022 - 01 - 06 - 21:22
 * @Package: com.zyj.gulimall.order.vo
 * @Description: 购物项
 */
@Data
public class OrderItemVo {

    /**
     * skuId
     */
    private Long skuId;

    /**
     * 是否被选中
     */
    private Boolean check;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片
     */
    private String image;

    /**
     * 套餐信息
     */
    private List<String> skuAttr;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer count;

    private BigDecimal totalPrice;



    /**
     * TODO: 重量（去仓库服务中查询）
     */
    private BigDecimal weight;

}
