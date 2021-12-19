package com.zyj.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName SpuItemAttrGroupVo
 * @author: YaJun
 * @Date: 2021 - 12 - 19 - 1:33
 * @Package: com.zyj.gulimall.product.vo
 * @Description: spu的规格参数（属性）
 */
@Data
@ToString
public class SpuItemAttrGroupVo {

    private String groupName;
    private List<Attr> attrs;

}
