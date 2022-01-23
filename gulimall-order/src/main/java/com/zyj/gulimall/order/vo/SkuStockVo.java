package com.zyj.gulimall.order.vo;

import lombok.Data;

/**
 * @program: gulimall
 * @ClassName SkuStockVo
 * @author: YaJun
 * @Date: 2022 - 01 - 12 - 20:49
 * @Package: com.zyj.gulimall.order.vo
 * @Description: 用来收集sku是否有库存
 */
@Data
public class SkuStockVo {

    private Long skuId;

    private Boolean hasStock;

}
