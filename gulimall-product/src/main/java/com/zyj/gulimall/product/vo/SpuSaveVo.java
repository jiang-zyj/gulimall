/**
  * Copyright 2021 bejson.com 
  */
package com.zyj.gulimall.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Auto-generated: 2021-11-18 21:18:9
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class SpuSaveVo {

    /**
     * pms_spu_info
     */
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;

    /**
     * pms_spu_info_desc
     */
    private List<String> decript;

    /**
     * pms_spu_images
     */
    private List<String> images;

    /**
     * gulimall_sms -> sms_spu_bounds
     */
    private Bounds bounds;

    /**
     * pms_product_attr_value
     */
    private List<BaseAttrs> baseAttrs;

    /**
     * pms_sku_info \ pms_sku_images \ pms_sku_sale_attr_value
     * gulimall_sms -> sms_sku_ladder \ sms_sku_full_reduction \ sms_member_price
     */
    private List<Skus> skus;


}