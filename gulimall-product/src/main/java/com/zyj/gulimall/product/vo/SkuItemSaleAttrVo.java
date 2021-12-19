package com.zyj.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SkuItemSaleAttrVo
 * @author: YaJun
 * @Date: 2021 - 12 - 19 - 1:34
 * @Package: com.zyj.gulimall.product.vo
 * @Description: sku的销售属性组合
 */
@Data
@ToString
public class SkuItemSaleAttrVo {

    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVo> attrValues;

}
