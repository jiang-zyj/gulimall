package com.zyj.gulimall.product.vo;

import com.zyj.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @program: gulimall
 * @ClassName AttrGroupWithAttrVo
 * @author: YaJun
 * @Date: 2021 - 11 - 18 - 20:01
 * @Package: com.zyj.gulimall.product.vo
 * @Description:
 */
@Data
public class AttrGroupWithAttrVo {

    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * 属性
     */
    private List<AttrEntity> attrs;

}
