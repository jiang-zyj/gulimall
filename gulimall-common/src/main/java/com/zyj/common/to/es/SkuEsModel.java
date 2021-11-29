package com.zyj.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @program: gulimall
 * @ClassName SkuEsModel
 * @author: YaJun
 * @Date: 2021 - 11 - 29 - 21:09
 * @Package: com.zyj.common.to.es
 * @Description:
 */
@Data
public class SkuEsModel {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long catelogId;

    private String brandName;

    private String brandImg;

    private String catelogName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;

        private String attrName;

        private String attrValue;
    }
}
