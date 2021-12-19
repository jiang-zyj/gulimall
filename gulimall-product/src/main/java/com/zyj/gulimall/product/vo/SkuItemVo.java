package com.zyj.gulimall.product.vo;

import com.zyj.gulimall.product.entity.SkuImagesEntity;
import com.zyj.gulimall.product.entity.SkuInfoEntity;
import com.zyj.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SkuItemVo
 * @author: YaJun
 * @Date: 2021 - 12 - 18 - 22:43
 * @Package: com.zyj.gulimall.product.vo
 * @Description: 商品详情数据
 */
@Data
public class SkuItemVo {

    // 1. sku基本信息获取 pms_sku_info
    private SkuInfoEntity info;

    //TODO: 是否有货，默认有货，这里逻辑没有做，以后可以加
    private boolean hasStock = true;

    // 2. sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;

    // 3. 获取sku的销售属性组合
    private List<SkuItemSaleAttrVo> saleAttr;

    // 4. 获取spu的介绍
    private SpuInfoDescEntity desc;

    // 5. 获取spu的规格参数（属性）
    List<SpuItemAttrGroupVo> groupAttrs;

}
